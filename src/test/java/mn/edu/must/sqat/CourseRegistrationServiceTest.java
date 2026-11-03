package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    CourseRegistrationService service;

    @Captor
    ArgumentCaptor<String> emailBodyCaptor;

    private static final String STUDENT_ID = "S001";
    private static final String COURSE_ID = "CS313";

    private Student activeStudent() {
        return new Student(STUDENT_ID, "student@must.edu.mn", true, List.of("CS201"));
    }

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    @Test
    void идэвхтэйОюутанБүртгүүлэхэд_OKБуцажХадгалжИмэйлИлгээнэ() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(
                eq("student@must.edu.mn"),
                eq("Бүртгэл баталгаажлаа"),
                contains(COURSE_ID));
    }

    @Test
    void байхгүйОюутанБүртгүүлэхэд_АлдааБуцаажХадгалахгүйИмэйлЯвуулахгүй() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void идэвхгүйОюутанБүртгүүлэхэд_АлдааБуцаажИмэйлтэйХарьцахгүй() {
        Student inactive = new Student(
                STUDENT_ID, "student@must.edu.mn", false, List.of("CS201"));
        when(repository.findStudent(STUDENT_ID)).thenReturn(inactive);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    @Test
    void өмнөхХичээлДутууБүртгүүлэхэд_АлдааБуцаажХадгалахгүй() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201", "CS202"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void имэйлИлгээгчАлдааШидэхэд_БүртгэлАмжилттайХэвээрХадгалагдана() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(anyString(), anyString(), anyString());
    }

    @Test
    void nullОролтӨгөхөд_IllegalArgumentExceptionШидэжХамаарлуудтайХарьцахгүй() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, COURSE_ID));
        assertThrows(IllegalArgumentException.class,
                () -> service.register(STUDENT_ID, null));

        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void амжилттайБүртгүүлэхэд_ИмэйлийнБиедХичээлийнКодАгуулна() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));

        service.register(STUDENT_ID, COURSE_ID);

        verify(emailSender).send(
                eq("student@must.edu.mn"),
                eq("Бүртгэл баталгаажлаа"),
                emailBodyCaptor.capture());
        assertEquals("CS313 хичээлд амжилттай бүртгэгдлээ.", emailBodyCaptor.getValue());
    }

    @ParameterizedTest(name = "{index}: {0} нөхцөлд {1} буцна")
    @CsvSource({
            "missing-student, ERROR_NO_STUDENT",
            "missing-course, ERROR_NO_COURSE",
            "missing-prerequisite, ERROR_PREREQUISITES"
    })
    void бүртгэлийнАлдааныНөхцөлд_ХүлээгдсэнАлдааныКодБуцна(
            String scenario, String expectedResult) {

        // Нэг параметржүүлсэн тестийн хувилбар бүрд ашиглагдахгүй
        // нийтлэг stub-уудыг lenient болгож strict stubs алдаанаас сэргийлэв.
        lenient().when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent());
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(true);
        lenient().when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));

        if ("missing-student".equals(scenario)) {
            when(repository.findStudent(STUDENT_ID)).thenReturn(null);
        } else if ("missing-course".equals(scenario)) {
            when(repository.courseExists(COURSE_ID)).thenReturn(false);
        } else if ("missing-prerequisite".equals(scenario)) {
            when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201", "CS202"));
        }

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals(expectedResult, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }
}
