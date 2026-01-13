package com.moirai.alloc.meeting.command.domain.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "agenda")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agenda_id")
    private Long agendaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingRecord meeting;

    @Column(name = "discussion_title", nullable = false, length = 40)
    private String discussionTitle;

    @Lob
    @Column(name = "discussion_content")
    private String discussionContent;

    @Lob
    @Column(name = "discussion_result")
    private String discussionResult;

    @Column(name = "agenda_type", length = 40)
    private String agendaType;

    public static Agenda create(MeetingRecord meeting,
                                String discussionTitle,
                                String discussionContent,
                                String discussionResult,
                                String agendaType) {
        return Agenda.builder()
                .meeting(meeting)
                .discussionTitle(discussionTitle)
                .discussionContent(discussionContent)
                .discussionResult(discussionResult)
                .agendaType(agendaType)
                .build();
    }

    @Builder
    private Agenda(MeetingRecord meeting,
                   String discussionTitle,
                   String discussionContent,
                   String discussionResult,
                   String agendaType) {
        this.meeting = meeting;
        this.discussionTitle = discussionTitle;
        this.discussionContent = discussionContent;
        this.discussionResult = discussionResult;
        this.agendaType = agendaType;
    }
}
