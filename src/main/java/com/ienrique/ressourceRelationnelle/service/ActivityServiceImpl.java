package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.ActivityMessageDto;
import com.ienrique.ressourceRelationnelle.dto.AnswerPollDto;
import com.ienrique.ressourceRelationnelle.dto.SendMessageDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
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
  private final ActivityMessageRepository activityMessageRepository;
  private final QuizRepository quizRepository;
  private final QuizQuestionRepository quizQuestionRepository;
  private final FriendRepository friendRepository;
  private final QuizParticipantAnswerRepository quizParticipantAnswerRepository;
  private final PollParticipantAnswerRepository pollParticipantAnswerRepository;
  private final PollRepository pollRepository;
  private final PollOptionRepository pollOptionRepository;
  private final AppUserRepository appUserRepository;
  private final UserService userService;
  private final ActivitySessionRepository activitySessionRepository;
  private final ActivityParticipantRepository activityParticipantRepository;
  private final QuizMapper quizMapper;
  private final PollMapper pollMapper;

  @Override
  @Transactional
  public ActivityResponseDto startActivity(UUID resourceId) {
    // récupération de la ressource interactive
    final InteractiveResource interactiveResource =
        interactiveResourceRepository
            .findByResourceResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    // récupération de l'utilisateur actuel
    final UserDto currentUser = userService.getCurrentUser();
    final AppUser user =
        appUserRepository
            .findById(currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    // création de la session
    final ActivitySession activitySession = new ActivitySession();
    activitySession.setStartedAt(Instant.now());
    activitySession.setInteractiveResource(interactiveResource);
    activitySession.setCreatedBy(user);
    activitySession.setStatus(ActivitySessionStatus.ACTIVE);
    // enregistrement de la session
    final ActivitySession savedSession = activitySessionRepository.save(activitySession);

    // création du participant ayant le role HOST
    final ActivityParticipant activityParticipant = new ActivityParticipant();
    activityParticipant.setActivitySession(savedSession);
    activityParticipant.setAppUser(user);
    activityParticipant.setStatus(ParticipantStatus.JOINED);
    activityParticipant.setRole(ParticipantRole.HOST);
    activityParticipant.setJoinedAt(Instant.now());
    // enregistrement du participant
    activityParticipantRepository.save(activityParticipant);

    // chargement de l'activité
    final ActivityResponseDto response = new ActivityResponseDto();
    response.setActivitySessionId(savedSession.getActivitySessionId());
    response.setActivityType(interactiveResource.getActivityType());

    // si type QUIZ on charge le quiz sinon sondage
    if (interactiveResource.getActivityType() == ActivityType.QUIZ) {
      response.setQuiz(loadQuiz(interactiveResource));
    } else if (interactiveResource.getActivityType() == ActivityType.POLL) {
      response.setPoll(loadPoll(interactiveResource));
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
  public CheckQuizAnswerResponseDto answerQuizQuestion(CheckQuizAnswerDto checkQuizAnswerDto) {
    // vérifier que l'utilisateur participe à la session
    final UserDto currentUser = userService.getCurrentUser();
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                checkQuizAnswerDto.getActivitySessionId(), currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    // récupérer la question
    final QuizQuestion question =
        quizQuestionRepository
            .findById(checkQuizAnswerDto.getQuizQuestionId())
            .orElseThrow(() -> new NotFoundException("Question not found"));

    // vérifier que pas déjà rep

    final boolean alreadyAnswer =
        quizParticipantAnswerRepository.existsByActivityParticipantAndQuizQuestion(
            participant, question);
    if (alreadyAnswer) {
      throw new BadRequestException("Question already answered");
    }

    // save réponse
    final QuizParticipantAnswer answer = new QuizParticipantAnswer();
    answer.setActivityParticipant(participant);
    answer.setUserAnswer(checkQuizAnswerDto.getUserAnswer());
    answer.setQuizQuestion(question);
    answer.setAnsweredAt(Instant.now());

    quizParticipantAnswerRepository.save(answer);

    // vérifie si réponse donnée par le user est la même que la réponse à la question
    final boolean isCorrect = question.isCorrectAnswer() == checkQuizAnswerDto.getUserAnswer();

    // construction de la réponse
    final CheckQuizAnswerResponseDto response = new CheckQuizAnswerResponseDto();
    response.setCorrect(isCorrect);

    return response;
  }

  @Override
  @Transactional
  public void answerPollOption(AnswerPollDto answerPollDto) {
    // vérifier que le user participe à la session
    final UserDto currentUser = userService.getCurrentUser();
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                answerPollDto.getActivitySessionId(), currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("Participant not found"));

    // récupérer l'option choisie
    final PollOption pollOption =
        pollOptionRepository
            .findById(answerPollDto.getPollOptionId())
            .orElseThrow(() -> new NotFoundException("Option not found"));

    // vérifier qu’il n’a pas déjà répondu à ce poll
    final boolean alreadyAnswered =
        pollParticipantAnswerRepository.existsByActivityParticipantAndPollOptionPoll(
            participant, pollOption.getPoll());
    if (alreadyAnswered) {
      throw new BadRequestException("Poll already answered");
    }

    // enregistrer la réponse
    final PollParticipantAnswer answer = new PollParticipantAnswer();
    answer.setAnsweredAt(Instant.now());
    answer.setPollOption(pollOption);
    answer.setActivityParticipant(participant);

    pollParticipantAnswerRepository.save(answer);
  }

  @Override
  @Transactional(readOnly = true)
  public int getQuizScore(UUID activitySessionId) {
    // vérifier que le user participe à la session
    final UserDto currentUser = userService.getCurrentUser();
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                activitySessionId, currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    // récupération des réponses
    final List<QuizParticipantAnswer> answers =
        quizParticipantAnswerRepository.findByActivityParticipant(participant);

    // calcul du score
    final int score =
        (int)
            answers.stream()
                .filter(
                    answer -> answer.getQuizQuestion().isCorrectAnswer() == answer.isUserAnswer())
                .count();
    return score;
  }

  @Override
  @Transactional
  public void inviteParticipant(UUID activitySessionId, UUID friendId) {
    // récupération de la session
    final ActivitySession session =
        activitySessionRepository
            .findById(activitySessionId)
            .orElseThrow(() -> new NotFoundException("Session not found"));

    // vérifier que l’utilisateur courant est bien le host de cette session
    final UserDto currentUser = userService.getCurrentUser();
    final ActivityParticipant hostParticipant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                activitySessionId, currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    if (hostParticipant.getRole() != ParticipantRole.HOST) {
      throw new BadRequestException("Only host can invite participants");
    }

    // récupérer l'ami invité
    final AppUser friend =
        appUserRepository
            .findById(friendId)
            .orElseThrow(() -> new NotFoundException("Friend not found"));

    if (friendId.equals(currentUser.getAppUserId())) {
      throw new BadRequestException("You cannot invite yourself");
    }

    final boolean isFriend =
        friendRepository.existsByRequesterUserAppUserIdAndReceiverUserAppUserId(
            currentUser.getAppUserId(), friendId);

    if (!isFriend) {
      throw new BadRequestException("You can only invite your friends");
    }

    // vérifier que l'ami n'est pas déjà dans la session
    final boolean alreadyParticipant =
        activityParticipantRepository.existsByActivitySessionAndAppUser(session, friend);

    if (alreadyParticipant) {
      throw new BadRequestException("User is already invited or already joined this session");
    }

    // création de l'invitation
    final ActivityParticipant invitedParticipant = new ActivityParticipant();
    invitedParticipant.setActivitySession(session);
    invitedParticipant.setAppUser(friend);
    invitedParticipant.setRole(ParticipantRole.PARTICIPANT);
    invitedParticipant.setStatus(ParticipantStatus.PENDING);
    invitedParticipant.setInvitedAt(Instant.now());

    activityParticipantRepository.save(invitedParticipant);
  }

  @Override
  @Transactional
  public void acceptInvitation(UUID activitySessionId) {
    // récupérer l'utilisateur
    final UserDto currentUser = userService.getCurrentUser();

    // retrouver son activityParticpant dans la session
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                activitySessionId, currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    // vérifier que le statut actuel est PENDING
    if (participant.getStatus() != ParticipantStatus.PENDING) {
      throw new BadRequestException("Invitation is not pending");
    }

    // passer le statut à JOINED
    participant.setStatus(ParticipantStatus.JOINED);
    participant.setJoinedAt(Instant.now());

    activityParticipantRepository.save(participant);
  }

  @Override
  @Transactional
  public void declineInvitation(UUID activitySessionId) {
    // récupérer l'utilisateur
    final UserDto currentUser = userService.getCurrentUser();

    // retrouver son activityParticpant dans la session
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                activitySessionId, currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

    // vérifier que le statut actuel est PENDING
    if (participant.getStatus() != ParticipantStatus.PENDING) {
      throw new BadRequestException("Invitation is not pending");
    }

    // passer le statut à DECLINED
    participant.setStatus(ParticipantStatus.DECLINED);

    activityParticipantRepository.save(participant);
  }

  @Override
  @Transactional
  public void sendMessage(UUID activitySessionId, SendMessageDto content) {
    // récupérer le user courant
    final UserDto currentUser = userService.getCurrentUser();

    // vérifie qu'il participe
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                activitySessionId, currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    if (participant.getStatus() != ParticipantStatus.JOINED) {
      throw new BadRequestException("User is not an active participant");
    }

    // crée un message
    final ActivitySession session = participant.getActivitySession();
    final ActivityMessage message = new ActivityMessage();
    message.setActivitySession(session);
    message.setContent(content.getContent());
    message.setSender(participant.getAppUser());
    message.setSentAt(Instant.now());

    activityMessageRepository.save(message);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ActivityMessageDto> getMessages(UUID activitySessionId) {
    // vérifier que l’utilisateur participe à la session
    final UserDto currentUser = userService.getCurrentUser();
    final ActivityParticipant participant =
        activityParticipantRepository
            .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
                activitySessionId, currentUser.getAppUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    if (participant.getStatus() != ParticipantStatus.JOINED) {
      throw new BadRequestException("User is not an active participant");
    }

    // récupérer tous les messages de la session
    final List<ActivityMessage> messages =
        activityMessageRepository.findByActivitySessionActivitySessionIdOrderBySentAtAsc(
            activitySessionId);

    // renvoyer une liste de DTO
    return messages.stream()
        .map(
            message -> {
              final ActivityMessageDto dto = new ActivityMessageDto();
              dto.setSenderUsername(message.getSender().getPseudo());
              dto.setActivityMessageId(message.getActivityMessageId());
              dto.setSenderId(message.getSender().getAppUserId());
              dto.setContent(message.getContent());
              dto.setSentAt(message.getSentAt());
              return dto;
            })
        .toList();
  }

  private StartQuizDto loadQuiz(InteractiveResource interactiveResource) {
    // récupération du quiz
    final Quiz quiz =
        quizRepository
            .findByInteractiveResourceInteractiveResourceId(
                interactiveResource.getInteractiveResourceId())
            .orElseThrow(() -> new NotFoundException("Quiz not found"));

    // création du quiz sans les réponses et mapping entity vers dto
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

  private PollDto loadPoll(InteractiveResource interactiveResource) {
    // récupération du sondage
    final Poll poll =
        pollRepository
            .findByInteractiveResourceInteractiveResourceId(
                interactiveResource.getInteractiveResourceId())
            .orElseThrow(() -> new NotFoundException("Poll not found"));

    return pollMapper.toDto(poll);
  }
}
