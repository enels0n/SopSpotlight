package net.enelson.sopspotlight.bukkit.spotlight;

public final class SpotlightPayload {

    private final String playerName;
    private final String originServerId;
    private final boolean sendAvatar;
    private final String senderText;

    public SpotlightPayload(String playerName, String originServerId, boolean sendAvatar, String senderText) {
        this.playerName = playerName == null ? "" : playerName;
        this.originServerId = originServerId == null ? "" : originServerId;
        this.sendAvatar = sendAvatar;
        this.senderText = senderText == null ? "" : senderText;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOriginServerId() {
        return originServerId;
    }

    public boolean isSendAvatar() {
        return sendAvatar;
    }

    public String getSenderText() {
        return senderText;
    }
}
