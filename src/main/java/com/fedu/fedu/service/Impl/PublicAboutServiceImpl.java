package com.fedu.fedu.service.Impl;

import com.fedu.fedu.dto.res.AboutStatsResponse;
import com.fedu.fedu.dto.res.AboutFeaturesResponse;
import com.fedu.fedu.entity.Subject;
import com.fedu.fedu.repository.ClassroomRepository;
import com.fedu.fedu.repository.SubjectRepository;
import com.fedu.fedu.repository.UserAccountRepository;
import com.fedu.fedu.service.PublicAboutService;
import com.fedu.fedu.utils.enums.UserRole;
import com.fedu.fedu.utils.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.fedu.fedu.dto.req.ContactRequest;
import com.fedu.fedu.service.MailService;
import com.fedu.fedu.entity.ContactMessage;
import com.fedu.fedu.repository.ContactMessageRepository;
import com.fedu.fedu.service.SystemConfigService;
import com.fedu.fedu.utils.enums.ContactStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicAboutServiceImpl implements PublicAboutService {

    private final UserAccountRepository userAccountRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final MailService mailService;
    private final ContactMessageRepository contactMessageRepository;
    private final SystemConfigService systemConfigService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void processContactMessage(ContactRequest contactRequest) {
        log.info("Processing contact message from {}: {}", contactRequest.getEmail(), contactRequest.getSubject());

        ContactMessage contactMessage = ContactMessage.builder()
                .name(contactRequest.getName())
                .email(contactRequest.getEmail())
                .subject(contactRequest.getSubject())
                .message(contactRequest.getMessage())
                .status(ContactStatus.PENDING)
                .build();
        contactMessageRepository.save(contactMessage);

        String adminEmail = systemConfigService.getSettingValue("MAIL_SENDER_EMAIL", "default@example.com");

        // 1. Send email to Admin
        String adminEmailContent = String.format(
                "<h3>Tin nhắn liên hệ mới từ FEdu</h3>" +
                        "<p><b>Họ tên:</b> %s</p>" +
                        "<p><b>Email:</b> %s</p>" +
                        "<p><b>Tiêu đề:</b> %s</p>" +
                        "<p><b>Nội dung:</b></p>" +
                        "<p>%s</p>",
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getSubject(),
                contactRequest.getMessage());

        try {
            mailService.sendEmail(adminEmail, "FEdu Contact: " + contactRequest.getSubject(),
                    adminEmailContent, null);
            log.info("Contact email sent successfully to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send contact email to admin: {}, error: {}", adminEmail, e.getMessage());
        }

        // 2. Send confirmation email to User
        String userEmailContent = String.format(
                "<h3>Xin chào %s,</h3>" +
                        "<p>Chúng tôi đã nhận được tin nhắn liên hệ của bạn với tiêu đề: <b>%s</b></p>" +
                        "<p>Đội ngũ của chúng tôi sẽ xem xét và phản hồi lại bạn sớm nhất có thể.</p>" +
                        "<p>Trân trọng,<br>Đội ngũ FEdu</p>",
                contactRequest.getName(),
                contactRequest.getSubject());

        try {
            mailService.sendEmail(contactRequest.getEmail(), "Xác nhận đã nhận liên hệ: " + contactRequest.getSubject(),
                    userEmailContent, null);
            log.info("Confirmation email sent successfully to user: {}", contactRequest.getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email to user: {}, error: {}", contactRequest.getEmail(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AboutStatsResponse getSystemStats() {
        log.info("Fetching system stats for public about page");
        long students = userAccountRepository.countByRoleAndStatus(UserRole.STUDENT, UserStatus.ACTIVE);
        long teachers = userAccountRepository.countByRoleAndStatus(UserRole.TEACHER, UserStatus.ACTIVE);
        long classrooms = classroomRepository.countByIsDeletedFalse();
        long subjects = subjectRepository.countByIsDeletedFalse();

        return AboutStatsResponse.builder()
                .totalStudents(students)
                .totalTeachers(teachers)
                .totalClassrooms(classrooms)
                .totalSubjects(subjects)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getFeaturedSubjects() {
        log.info("Fetching featured subjects for public about page");
        return subjectRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public AboutFeaturesResponse getFeaturesStats() {
        log.info("Fetching features stats for public page");

        long totalPaths = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM learning_paths WHERE is_deleted = false OR is_deleted IS NULL").getSingleResult())
                .longValue();

        long totalMaterials = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM node_materials WHERE is_deleted = false OR is_deleted IS NULL").getSingleResult())
                .longValue();

        long totalSubMentors = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT sub_mentor_id) FROM classroom_sub_mentor").getSingleResult()).longValue();

        long totalClassrooms = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM classrooms WHERE is_deleted = false OR is_deleted IS NULL").getSingleResult())
                .longValue();

        long totalSubmissions = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM submissions WHERE is_deleted = false OR is_deleted IS NULL").getSingleResult())
                .longValue();

        long totalQuestions = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM node_questions WHERE is_deleted = false OR is_deleted IS NULL").getSingleResult())
                .longValue();

        List<Object[]> pathsRaw = entityManager.createNativeQuery(
                "SELECT lp.path_id, lp.path_name, s.subject_code " +
                        "FROM learning_paths lp " +
                        "LEFT JOIN subjects s ON lp.subject_id = s.subject_id " +
                        "WHERE lp.is_deleted = false OR lp.is_deleted IS NULL")
                .getResultList();
        List<AboutFeaturesResponse.LearningPathDto> learningPathList = pathsRaw.stream()
                .map(row -> AboutFeaturesResponse.LearningPathDto.builder()
                        .pathId(((Number) row[0]).longValue())
                        .pathName((String) row[1])
                        .subjectCode((String) row[2])
                        .build())
                .collect(Collectors.toList());

        List<Object[]> classroomsRaw = entityManager.createNativeQuery(
                "SELECT c.classroom_id, c.class_name, s.term, s.academic_year " +
                        "FROM classrooms c " +
                        "LEFT JOIN semesters s ON c.semester_id = s.semester_id " +
                        "WHERE c.is_deleted = false OR c.is_deleted IS NULL")
                .getResultList();
        List<AboutFeaturesResponse.ClassroomDto> classroomList = classroomsRaw.stream()
                .map(row -> AboutFeaturesResponse.ClassroomDto.builder()
                        .classroomId(((Number) row[0]).longValue())
                        .className((String) row[1])
                        .semester(buildSemesterLabel((String) row[2], (Number) row[3]))
                        .build())
                .collect(Collectors.toList());

        List<Object[]> materialsRaw = entityManager.createNativeQuery(
                "SELECT material_id, title " +
                        "FROM node_materials " +
                        "WHERE is_deleted = false OR is_deleted IS NULL")
                .getResultList();
        List<AboutFeaturesResponse.MaterialDto> materialList = materialsRaw.stream()
                .map(row -> AboutFeaturesResponse.MaterialDto.builder()
                        .materialId(((Number) row[0]).longValue())
                        .title((String) row[1])
                        .build())
                .collect(Collectors.toList());

        List<Object[]> questionsRaw = entityManager.createNativeQuery(
                "SELECT nq.question_id, nq.content, ua.first_name " +
                        "FROM node_questions nq " +
                        "LEFT JOIN user_account ua ON nq.student_id = ua.user_id " +
                        "WHERE nq.is_deleted = false OR nq.is_deleted IS NULL " +
                        "ORDER BY nq.created_at DESC LIMIT 5")
                .getResultList();
        List<AboutFeaturesResponse.QuestionDto> questionList = questionsRaw.stream()
                .map(row -> AboutFeaturesResponse.QuestionDto.builder()
                        .questionId(((Number) row[0]).longValue())
                        .content((String) row[1])
                        .studentName((String) row[2])
                        .build())
                .collect(Collectors.toList());

        return AboutFeaturesResponse.builder()
                .totalPaths(totalPaths)
                .totalMaterials(totalMaterials)
                .totalSubMentors(totalSubMentors)
                .totalClassrooms(totalClassrooms)
                .totalSubmissions(totalSubmissions)
                .totalQuestions(totalQuestions)
                .learningPaths(learningPathList)
                .classrooms(classroomList)
                .materials(materialList)
                .questions(questionList)
                .build();
    }

    /** Dựng nhãn "Kì học" từ cột term (tên enum) + năm học, ví dụ "Fall 2024". */
    private static String buildSemesterLabel(String termRaw, Number academicYear) {
        if (termRaw == null || termRaw.isBlank()) {
            return null;
        }
        String label;
        try {
            label = com.fedu.fedu.utils.enums.Term.valueOf(termRaw.trim().toUpperCase()).getLabel();
        } catch (IllegalArgumentException ex) {
            label = termRaw;
        }
        return academicYear != null ? label + " " + academicYear.intValue() : label;
    }
}
