package com.daewoo.lms.training.educationCourseCategory.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "EDUCATION_COURSE_CATEGORY")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationCourseCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EDUCATION_CATEGORY_ID")
	private Long educationCategoryId;

	@Column(name = "CATEGORY_NAME", nullable = false, length = 150)
	private String categoryName;

	@Column(name = "SORT_ORDER", nullable = false)
	private Integer sortOrder;

	@Column(name = "HIDDEN_YN", nullable = false, length = 1)
	private String hiddenYn;

	@Column(name = "USE_YN", nullable = false, length = 1)
	private String useYn;

	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@Column(name = "CREATED_BY")
	private Long createdBy;

	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	@Column(name = "UPDATED_BY")
	private Long updatedBy;

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();

		this.createdAt = now;
		this.updatedAt = now;

		if (this.sortOrder == null) {
			this.sortOrder = 0;
		}

		if (this.hiddenYn == null) {
			this.hiddenYn = "N";
		}

		if (this.useYn == null) {
			this.useYn = "Y";
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}