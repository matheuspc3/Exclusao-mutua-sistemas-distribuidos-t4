package teste;

class RequestMessage {
    private String type;
    private int processId;

    public RequestMessage(String type, int processId) {
        this.type = type;
        this.processId = processId;
    }

    public String getType() {
        return type;
    }

    public int getProcessId() {
        return processId;
    }
}