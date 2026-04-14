package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.*;
import com.ienrique.ressourceRelationnelle.dto.activity.*;
import com.ienrique.ressourceRelationnelle.entity.*;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.PollMapper;
import com.ienrique.ressourceRelationnelle.mapper.QuizMapper;
import com.ienrique.ressourceRelationnelle.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

  private final ResourceRepository resourceRepository;
  private final InteractiveResourceRepository interactiveResourceRepository;
  private final QuizRepository quizRepository;
  private final PollRepository pollRepository;
  private final AppUserRepository appUserRepository;
  private final UserService userService;
  private final ActivitySessionRepository activitySessionRepository;
  private final QuizMapper quizMapper;
  private final PollMapper pollMapper;

  @Override
  @Transactional
  public ActivityResponseDto startActivity(UUID resourceId) {
    // on vérifie et récupère la ressource interactive
    final InteractiveResource interactiveResource =
        interactiveResourceRepository
            .findByResourceResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Interactive resource not found"));

    // création de l'objet contenant un type
    final ActivityResponseDto response = new ActivityResponseDto();
    response.setActivityType(interactiveResource.getActivityType());

    // si type quiz
    if (interactiveResource.getActivityType() == ActivityType.QUIZ) {
      // création de la session quiz
      response.setQuiz(startQuiz(interactiveResource));
    } else if (interactiveResource.getActivityType() == ActivityType.POLL) {
      // création de la session sondage
      response.setPoll(startPoll(interactiveResource));
    } else {
      // exception l'activité n'existe pas
      throw new NotFoundException("Activity type not found");
    }

    return response;
  }

  @Override
  @Transactional
  public QuizDto createQuiz(CreateQuizDto createQuizDto) {
    final Resource resource =
        resourceRepository
            .findById(createQuizDto.getResourceId())
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    if (resource.getResourceType() != ResourceType.ACTIVITY_GAME) {
      throw new BadRequestException("Resource is not an activity game");
    }

    if (interactiveResourceRepository
        .findByResourceResourceId(resource.getResourceId())
        .isPresent()) {
      throw new BadRequestException("Interactive resource already exists for this resource");
    }

    // création de la ressource interactive
    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setActivityType(ActivityType.QUIZ);
    interactiveResource.setResource(resource);

    final InteractiveResource savedInteractiveResource =
        interactiveResourceRepository.save(interactiveResource);

    // création du quiz
    final Quiz quiz = new Quiz();
    quiz.setCreatedAt(Instant.now());
    quiz.setInteractiveResource(savedInteractiveResource);

    // création des questions
    final List<QuizQuestion> quizQuestions =
        createQuizDto.getQuestions().stream()
            .map(
                q -> {
                  final QuizQuestion question = new QuizQuestion();
                  question.setQuestion(q.getQuestion());
                  question.setCorrectAnswer(q.isCorrectAnswer());
                  question.setQuiz(quiz);
                  return question;
                })
            .toList();

    quiz.setQuestions(quizQuestions);

    final Quiz savedQuiz = quizRepository.save(quiz);

    return quizMapper.toDto(savedQuiz);
  }

  @Override
  @Transactional
  public PollDto createPoll(CreatePollDto createPollDto) {
    final Resource resource =
        resourceRepository
            .findById(createPollDto.getResourceId())
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    if (resource.getResourceType() != ResourceType.ACTIVITY_GAME) {
      throw new BadRequestException("Resource is not an activity game");
    }

    if (interactiveResourceRepository
        .findByResourceResourceId(resource.getResourceId())
        .isPresent()) {
      throw new BadRequestException("Interactive resource already exists for this resource");
    }

    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setActivityType(ActivityType.POLL);
    interactiveResource.setResource(resource);

    final InteractiveResource savedInteractiveResource =
        interactiveResourceRepository.save(interactiveResource);

    final Poll poll = new Poll();
    poll.setCreatedAt(Instant.now());
    poll.setQuestion(createPollDto.getQuestion());
    poll.setInteractiveResource(savedInteractiveResource);

    final List<PollOption> pollOptions =
        createPollDto.getOptions().stream()
            .map(
                optionLabel -> {
                  PollOption pollOption = new PollOption();
                  pollOption.setOptionLabel(optionLabel);
                  pollOption.setPoll(poll);
                  return pollOption;
                })
            .toList();

    poll.setOptions(pollOptions);

    final Poll savedPoll = pollRepository.save(poll);

    return pollMapper.toDto(savedPoll);
  }

  @Override
  @Transactional
  public CheckQuizAnswerResponseDto checkQuizAnswer(CheckQuizAnswerDto request) {
    final Quiz quiz =
        quizRepository
            .findByQuestionsQuizQuestionId(request.getQuizQuestionId())
            .orElseThrow(() -> new NotFoundException("Quiz not found"));

    // récupération de la question
    final QuizQuestion question =
        quiz.getQuestions().stream()
            .filter(q -> q.getQuizQuestionId().equals(request.getQuizQuestionId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Question not found"));

    final CheckQuizAnswerResponseDto response = new CheckQuizAnswerResponseDto();
    // vérification de la réponse de l'utilisateur
    response.setCorrect(question.isCorrectAnswer() == request.getUserAnswer());

    return response;
  }

  private ActivitySession createActivitySession(InteractiveResource interactiveResource) {
    // on récupère l'utilisateur actuel
    final UserDto currentUser = userService.getCurrentUser();
    // on vérifie que l'utilisateur existe
    final AppUser user =
        appUserRepository
            .findById(currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    // création d'une session d'activité
    final ActivitySession session = new ActivitySession();
    session.setInteractiveResource(interactiveResource);
    session.setAppUser(user);
    session.setStartedAt(Instant.now());

    // enregistrement en base
    return activitySessionRepository.save(session);
  }

  private StartQuizDto startQuiz(InteractiveResource interactiveResource) {
    createActivitySession(interactiveResource);

    final Quiz quiz =
        quizRepository
            .findByInteractiveResourceInteractiveResourceId(
                interactiveResource.getInteractiveResourceId())
            .orElseThrow(() -> new NotFoundException("Quiz not found"));

    final StartQuizDto dto = new StartQuizDto();
    dto.setQuizId(quiz.getQuizId());
    dto.setResourceId(interactiveResource.getResource().getResourceId());
    dto.setCreatedAt(quiz.getCreatedAt());
    dto.setQuestions(
        quiz.getQuestions().stream()
            .map(
                question -> {
                  final StartQuizQuestionDto questionDto = new StartQuizQuestionDto();
                  questionDto.setQuestion(question.getQuestion());
                  return questionDto;
                })
            .toList());

    return dto;
  }

  private PollDto startPoll(InteractiveResource interactiveResource) {
    createActivitySession(interactiveResource);

    final Poll poll =
        pollRepository
            .findByInteractiveResourceInteractiveResourceId(
                interactiveResource.getInteractiveResourceId())
            .orElseThrow(() -> new NotFoundException("Poll not found"));

    return pollMapper.toDto(poll);
  }
}
