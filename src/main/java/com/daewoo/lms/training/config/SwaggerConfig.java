package com.daewoo.lms.training.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
            new Info().title("Daewoo LMS API").description(
                """
                    교육과정·입학접수·NCS 평가 관리 API입니다.
                    
                    **사용 순서**: 분류/선택 목록 조회 → 과정·기수 선택 → 해당 업무 등록.
                    
                    각 API를 펼치면 처리 방식, 입력 조건, 요청 예시를 확인할 수 있습니다.
                    요청/응답의 Schema에는 필드별 한글 설명과 코드값이 표시됩니다.
                    예시의 ID는 실제 목록 조회에서 확인한 ID로 변경하세요.
                    
                    **주의할 차이**: 실제 삭제, 상태 취소, 비활성화는 API마다 다릅니다.
                    배열 생략/null과 빈 배열([])의 처리도 각 API 설명을 확인하세요.
                    """
            ).version("v1")
        );
    }
}
