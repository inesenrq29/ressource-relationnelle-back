package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.dto.activity.*;
import com.ienrique.ressourceRelationnelle.entity.*;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.PollMapper;
import com.ienrique.ressourceRelationnelle.mapper.QuizMapper;
import com.ienrique.ressourceRelationnelle.repository.*;
import com.ienrique.ressourceRelationnelle.service.ActivityServiceImpl;
import com.ienrique.ressourceRelationnelle.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

  @Mock private QuizRepository quizRepository;
  @Mock private AppUserRepository appUserRepository;
  @Mock private InteractiveResourceRepository interactiveResourceRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private ActivitySessionRepository activitySessionRepository;
  @Mock private PollRepository pollRepository;
  @Mock private QuizMapper quizMapper;
  @Mock private PollMapper pollMapper;
  @Mock private UserService userService;

  @InjectMocks private ActivityServiceImpl activityService;

  @Test
  @DisplayName("Should start quiz activity successfully")
  void shouldStartQuizActivity() {
    final UUID resourceId = UUID.randomUUID();
    final UUID interactiveResourceId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);

    final UserDto currentUser = new UserDto();
    currentUser.setAppUserId(userId);

    final AppUser user = new AppUser();
    user.setAppUserId(userId);

    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setInteractiveResourceId(interactiveResourceId);
    interactiveResource.setActivityType(ActivityType.QUIZ);
    interactiveResource.setResource(resource);

    final Quiz quiz = new Quiz();
    quiz.setQuizId(UUID.randomUUID());
    quiz.setCreatedAt(Instant.now());
    quiz.setInteractiveResource(interactiveResource);
    quiz.setQuestions(List.of());

    final StartQuizDto startQuizDto = new StartQuizDto();
    startQuizDto.setQuizId(quiz.getQuizId());
    startQuizDto.setResourceId(resourceId);
    startQuizDto.setCreatedAt(quiz.getCreatedAt());
    startQuizDto.setQuestions(List.of());

    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.of(interactiveResource));

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

    when(activitySessionRepository.save(any(ActivitySession.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(quizRepository.findByInteractiveResourceInteractiveResourceId(interactiveResourceId))
        .thenReturn(Optional.of(quiz));

    final ActivityResponseDto result = activityService.startActivity(resourceId);

    assertEquals(ActivityType.QUIZ, result.getActivityType());
    assertNotNull(result.getQuiz());
    assertNull(result.getPoll());
  }

  @Test
  @DisplayName("Should start poll activity successfully")
  void shouldStartPollActivity() {
    final UUID resourceId = UUID.randomUUID();
    final UUID interactiveResourceId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);

    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setInteractiveResourceId(interactiveResourceId);
    interactiveResource.setActivityType(ActivityType.POLL);
    interactiveResource.setResource(resource);

    final UserDto currentUser = new UserDto();
    currentUser.setAppUserId(userId);

    final AppUser user = new AppUser();
    user.setAppUserId(userId);

    final Poll poll = new Poll();
    poll.setPollId(UUID.randomUUID());
    poll.setQuestion("Question test");
    poll.setCreatedAt(Instant.now());
    poll.setInteractiveResource(interactiveResource);
    poll.setOptions(List.of());

    final PollDto pollDto = new PollDto();
    pollDto.setPollId(poll.getPollId());
    pollDto.setResourceId(resourceId);
    pollDto.setQuestion(poll.getQuestion());
    pollDto.setCreatedAt(poll.getCreatedAt());
    pollDto.setOptions(List.of());

    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.of(interactiveResource));

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

    when(activitySessionRepository.save(any(ActivitySession.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(pollRepository.findByInteractiveResourceInteractiveResourceId(interactiveResourceId))
        .thenReturn(Optional.of(poll));

    when(pollMapper.toDto(poll)).thenReturn(pollDto);

    final ActivityResponseDto result = activityService.startActivity(resourceId);

    assertEquals(ActivityType.POLL, result.getActivityType());
    assertNotNull(result.getPoll());
    assertEquals(pollDto, result.getPoll());
    assertNull(result.getQuiz());
  }

  @Test
  @DisplayName("Should throw activity not found")
  void shouldThrowNotFoundActivity() {
    final UUID resourceId = UUID.randomUUID();
    final UUID interactiveResourceId = UUID.randomUUID();

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);

    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setInteractiveResourceId(interactiveResourceId);
    interactiveResource.setActivityType(null);
    interactiveResource.setResource(resource);

    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.of(interactiveResource));

    assertThrows(NotFoundException.class, () -> activityService.startActivity(resourceId));
  }

  @Test
  @DisplayName("Should throw when interactive resource is not found")
  void shouldThrowWhenInteractiveResourceNotFound() {
    final UUID resourceId = UUID.randomUUID();

    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> activityService.startActivity(resourceId));
  }

  ///
  @Test
  @DisplayName("Should create poll successfully")
  void shouldCreatePoll() {
    final UUID resourceId = UUID.randomUUID();
    final UUID interactiveResourceId = UUID.randomUUID();
    final UUID pollId = UUID.randomUUID();

    final CreatePollDto requestDto = new CreatePollDto();
    requestDto.setResourceId(resourceId);
    requestDto.setQuestion("Quelle option préférez-vous ?");
    requestDto.setOptions(List.of("Option 1", "Option 2"));

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);
    resource.setResourceType(ResourceType.ACTIVITY_GAME);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.empty());

    final InteractiveResource savedInteractiveResource = new InteractiveResource();
    savedInteractiveResource.setInteractiveResourceId(interactiveResourceId);
    savedInteractiveResource.setActivityType(ActivityType.POLL);
    savedInteractiveResource.setResource(resource);

    when(interactiveResourceRepository.save(any(InteractiveResource.class)))
        .thenReturn(savedInteractiveResource);

    final Poll savedPoll = new Poll();
    savedPoll.setPollId(pollId);
    savedPoll.setQuestion(requestDto.getQuestion());
    savedPoll.setCreatedAt(Instant.now());
    savedPoll.setInteractiveResource(savedInteractiveResource);

    final List<PollOption> savedOptions =
        requestDto.getOptions().stream()
            .map(
                optionLabel -> {
                  final PollOption option = new PollOption();
                  option.setOptionLabel(optionLabel);
                  option.setPoll(savedPoll);
                  return option;
                })
            .toList();

    savedPoll.setOptions(savedOptions);

    when(pollRepository.save(any(Poll.class))).thenReturn(savedPoll);

    final PollDto pollDto = new PollDto();
    pollDto.setPollId(pollId);
    pollDto.setResourceId(resourceId);
    pollDto.setQuestion(requestDto.getQuestion());
    pollDto.setCreatedAt(savedPoll.getCreatedAt());
    pollDto.setOptions(
        requestDto.getOptions().stream()
            .map(
                optionLabel -> {
                  final PollOptionDto dto = new PollOptionDto();
                  dto.setOptionLabel(optionLabel);
                  return dto;
                })
            .toList());

    when(pollMapper.toDto(savedPoll)).thenReturn(pollDto);

    final PollDto result = activityService.createPoll(requestDto);

    assertEquals(pollId, result.getPollId());
    assertEquals(resourceId, result.getResourceId());
    assertEquals("Quelle option préférez-vous ?", result.getQuestion());
    assertNotNull(result.getOptions());
    assertEquals(2, result.getOptions().size());
    assertEquals("Option 1", result.getOptions().get(0).getOptionLabel());
    assertEquals("Option 2", result.getOptions().get(1).getOptionLabel());
  }

  @Test
  @DisplayName("Should throw when resource is not found")
  void shouldThrowWhenResourceNotFound() {
    final UUID resourceId = UUID.randomUUID();

    final CreatePollDto requestDto = new CreatePollDto();
    requestDto.setResourceId(resourceId);
    requestDto.setQuestion("Quelle option préférez-vous ?");
    requestDto.setOptions(List.of("Option 1", "Option 2"));

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> activityService.createPoll(requestDto));
  }

  @Test
  @DisplayName("Should throw when resource is not an activity game")
  void shouldThrowWhenResourceIsNotActivityGame() {
    final UUID resourceId = UUID.randomUUID();

    final CreatePollDto requestDto = new CreatePollDto();
    requestDto.setResourceId(resourceId);
    requestDto.setQuestion("Quelle option préférez-vous ?");
    requestDto.setOptions(List.of("Option 1", "Option 2"));

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);
    resource.setResourceType(ResourceType.READING_SHEET);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

    assertThrows(BadRequestException.class, () -> activityService.createPoll(requestDto));
  }

  @Test
  @DisplayName("Should throw when interactive resource already exists")
  void shouldThrowWhenInteractiveResourceAlreadyExists() {
    final UUID resourceId = UUID.randomUUID();

    final CreatePollDto requestDto = new CreatePollDto();
    requestDto.setResourceId(resourceId);
    requestDto.setQuestion("Quelle option préférez-vous ?");
    requestDto.setOptions(List.of("Option 1", "Option 2"));

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);
    resource.setResourceType(ResourceType.ACTIVITY_GAME);

    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setInteractiveResourceId(UUID.randomUUID());
    interactiveResource.setActivityType(ActivityType.POLL);
    interactiveResource.setResource(resource);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.of(interactiveResource));

    assertThrows(BadRequestException.class, () -> activityService.createPoll(requestDto));
  }

  @Test
  @DisplayName("Should create quiz successfully")
  void shouldCreateQuiz() {
    final UUID resourceId = UUID.randomUUID();
    final UUID interactiveResourceId = UUID.randomUUID();
    final UUID quizId = UUID.randomUUID();

    final CreateQuizDto requestDto = new CreateQuizDto();
    requestDto.setResourceId(resourceId);

    final QuizQuestionDto question1 = new QuizQuestionDto();
    question1.setQuestion("La communication est importante ?");
    question1.setCorrectAnswer(true);

    final QuizQuestionDto question2 = new QuizQuestionDto();
    question2.setQuestion("Ignorer l'autre améliore la relation ?");
    question2.setCorrectAnswer(false);

    requestDto.setQuestions(List.of(question1, question2));

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);
    resource.setResourceType(ResourceType.ACTIVITY_GAME);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.empty());

    final InteractiveResource savedInteractiveResource = new InteractiveResource();
    savedInteractiveResource.setInteractiveResourceId(interactiveResourceId);
    savedInteractiveResource.setActivityType(ActivityType.QUIZ);
    savedInteractiveResource.setResource(resource);

    when(interactiveResourceRepository.save(any(InteractiveResource.class)))
        .thenReturn(savedInteractiveResource);

    final Quiz savedQuiz = new Quiz();
    savedQuiz.setQuizId(quizId);
    savedQuiz.setCreatedAt(Instant.now());
    savedQuiz.setInteractiveResource(savedInteractiveResource);

    final List<QuizQuestion> savedQuestions =
        requestDto.getQuestions().stream()
            .map(
                q -> {
                  final QuizQuestion question = new QuizQuestion();
                  question.setQuestion(q.getQuestion());
                  question.setCorrectAnswer(q.isCorrectAnswer());
                  question.setQuiz(savedQuiz);
                  return question;
                })
            .toList();

    savedQuiz.setQuestions(savedQuestions);

    when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);

    final QuizDto quizDto = new QuizDto();
    quizDto.setQuizId(quizId);
    quizDto.setResourceId(resourceId);
    quizDto.setCreatedAt(savedQuiz.getCreatedAt());
    quizDto.setQuestions(List.of(question1, question2));

    when(quizMapper.toDto(savedQuiz)).thenReturn(quizDto);

    final QuizDto result = activityService.createQuiz(requestDto);

    assertEquals(quizId, result.getQuizId());
    assertEquals(resourceId, result.getResourceId());
    assertNotNull(result.getQuestions());
    assertEquals(2, result.getQuestions().size());
    assertEquals("La communication est importante ?", result.getQuestions().get(0).getQuestion());
    assertTrue(result.getQuestions().get(0).isCorrectAnswer());
    assertEquals(
        "Ignorer l'autre améliore la relation ?", result.getQuestions().get(1).getQuestion());
    assertFalse(result.getQuestions().get(1).isCorrectAnswer());
  }

  @Test
  @DisplayName("Should throw when resource is not found for quiz creation")
  void shouldThrowWhenResourceNotFoundForQuizCreation() {
    final UUID resourceId = UUID.randomUUID();

    final CreateQuizDto requestDto = new CreateQuizDto();
    requestDto.setResourceId(resourceId);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> activityService.createQuiz(requestDto));
  }

  @Test
  @DisplayName("Should throw when resource is not an activity game for quiz creation")
  void shouldThrowWhenResourceIsNotActivityGameForQuizCreation() {
    final UUID resourceId = UUID.randomUUID();

    final CreateQuizDto requestDto = new CreateQuizDto();
    requestDto.setResourceId(resourceId);

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);
    resource.setResourceType(ResourceType.READING_SHEET);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

    assertThrows(BadRequestException.class, () -> activityService.createQuiz(requestDto));
  }

  @Test
  @DisplayName("Should throw when interactive resource already exists for quiz creation")
  void shouldThrowWhenInteractiveResourceAlreadyExistsForQuizCreation() {
    final UUID resourceId = UUID.randomUUID();

    final CreateQuizDto requestDto = new CreateQuizDto();
    requestDto.setResourceId(resourceId);

    final Resource resource = new Resource();
    resource.setResourceId(resourceId);
    resource.setResourceType(ResourceType.ACTIVITY_GAME);

    final InteractiveResource interactiveResource = new InteractiveResource();
    interactiveResource.setInteractiveResourceId(UUID.randomUUID());
    interactiveResource.setActivityType(ActivityType.QUIZ);
    interactiveResource.setResource(resource);

    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
    when(interactiveResourceRepository.findByResourceResourceId(resourceId))
        .thenReturn(Optional.of(interactiveResource));

    assertThrows(BadRequestException.class, () -> activityService.createQuiz(requestDto));
  }

  @Test
  @DisplayName("Should return true when quiz answer is correct")
  void shouldReturnTrueWhenQuizAnswerIsCorrect() {
    final UUID quizQuestionId = UUID.randomUUID();

    final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
    requestDto.setQuizQuestionId(quizQuestionId);
    requestDto.setUserAnswer(true);

    final QuizQuestion question = new QuizQuestion();
    question.setQuizQuestionId(quizQuestionId);
    question.setQuestion("La communication est importante ?");
    question.setCorrectAnswer(true);

    final Quiz quiz = new Quiz();
    quiz.setQuizId(UUID.randomUUID());
    quiz.setQuestions(List.of(question));

    when(quizRepository.findByQuestionsQuizQuestionId(quizQuestionId))
        .thenReturn(Optional.of(quiz));

    final CheckQuizAnswerResponseDto result = activityService.checkQuizAnswer(requestDto);

    assertTrue(result.isCorrect());
  }

  @Test
  @DisplayName("Should return false when quiz answer is incorrect")
  void shouldReturnFalseWhenQuizAnswerIsIncorrect() {
    final UUID quizQuestionId = UUID.randomUUID();

    final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
    requestDto.setQuizQuestionId(quizQuestionId);
    requestDto.setUserAnswer(false);

    final QuizQuestion question = new QuizQuestion();
    question.setQuizQuestionId(quizQuestionId);
    question.setQuestion("La communication est importante ?");
    question.setCorrectAnswer(true);

    final Quiz quiz = new Quiz();
    quiz.setQuizId(UUID.randomUUID());
    quiz.setQuestions(List.of(question));

    when(quizRepository.findByQuestionsQuizQuestionId(quizQuestionId))
        .thenReturn(Optional.of(quiz));

    final CheckQuizAnswerResponseDto result = activityService.checkQuizAnswer(requestDto);

    assertFalse(result.isCorrect());
  }

  @Test
  @DisplayName("Should throw when quiz is not found for answer check")
  void shouldThrowWhenQuizIsNotFoundForAnswerCheck() {
    final UUID quizQuestionId = UUID.randomUUID();

    final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
    requestDto.setQuizQuestionId(quizQuestionId);
    requestDto.setUserAnswer(true);

    when(quizRepository.findByQuestionsQuizQuestionId(quizQuestionId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> activityService.checkQuizAnswer(requestDto));
  }

  @Test
  @DisplayName("Should throw when question is not found in quiz for answer check")
  void shouldThrowWhenQuestionIsNotFoundInQuizForAnswerCheck() {
    final UUID requestedQuestionId = UUID.randomUUID();
    final UUID anotherQuestionId = UUID.randomUUID();

    final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
    requestDto.setQuizQuestionId(requestedQuestionId);
    requestDto.setUserAnswer(true);

    final QuizQuestion question = new QuizQuestion();
    question.setQuizQuestionId(anotherQuestionId);
    question.setQuestion("Question différente");
    question.setCorrectAnswer(true);

    final Quiz quiz = new Quiz();
    quiz.setQuizId(UUID.randomUUID());
    quiz.setQuestions(List.of(question));

    when(quizRepository.findByQuestionsQuizQuestionId(requestedQuestionId))
        .thenReturn(Optional.of(quiz));

    assertThrows(NotFoundException.class, () -> activityService.checkQuizAnswer(requestDto));
  }
}
