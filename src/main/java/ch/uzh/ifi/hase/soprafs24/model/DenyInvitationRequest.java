package ch.uzh.ifi.hase.soprafs24.model;

public class DenyInvitationRequest {
        private final long sessionId;
        private final long userId;
        private final long requestId;

        public DenyInvitationRequest(long sessionId, long userId, long requestId) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.requestId = requestId;
        }
        public long getSessionId() { return sessionId; }

        public long getUserId() { return userId; }

        public long getRequestId() { return requestId; }

}
