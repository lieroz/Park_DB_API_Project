package db_project.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorView {
    private String message;

    public ErrorView(@JsonProperty("message") final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
