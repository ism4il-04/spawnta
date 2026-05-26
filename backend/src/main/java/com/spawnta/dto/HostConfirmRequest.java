package com.spawnta.dto;

import java.util.List;

public class HostConfirmRequest {
    private List<Long> participantIds;

    public HostConfirmRequest() {}

    public HostConfirmRequest(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public List<Long> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
}
