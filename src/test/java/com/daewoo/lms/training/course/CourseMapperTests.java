package com.daewoo.lms.training.course;

import com.daewoo.lms.training.course.dto.CourseBatchDTO;
import com.daewoo.lms.training.course.dto.CourseDTO;
import com.daewoo.lms.training.course.mapper.CourseMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CourseMapperTests {
    @Test
    void allInterfaceMethodsHaveMappedStatementsAndDynamicSqlBinds() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/course/course.xml";
        try (var stream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        String prefix = CourseMapper.class.getName() + ".";
        for (var method : CourseMapper.class.getDeclaredMethods()) {
            assertTrue(configuration.hasStatement(prefix + method.getName()), method.getName());
        }
        CourseBatchDTO dto = new CourseBatchDTO();
        dto.setCourseId(1L);
        dto.setBatchId(2L);
        String insert = configuration.getMappedStatement(prefix + "insertBatch").getBoundSql(dto).getSql();
        assertTrue(insert.contains("COURSE_ID"));
        assertTrue(insert.contains("BATCH_ID"));
        assertFalse(insert.contains("TUITION_VISIBLE_YN"), "null fields must preserve database defaults");
        CourseDTO course = new CourseDTO();
        course.setCourseId(1L);
        String update = configuration.getMappedStatement(prefix + "updateCourse").getBoundSql(course).getSql();
        assertFalse(update.matches("(?s).*,\\s*WHERE.*"));
        for (String type : new String[]{"trainingJobCategory", "educationCategory", "campus", "instructor"}) {
            String sql = configuration.getMappedStatement(prefix + "existsReference")
                    .getBoundSql(Map.of("type", type, "id", 1L)).getSql();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.trim().endsWith("= ?"));
        }
    }
}
