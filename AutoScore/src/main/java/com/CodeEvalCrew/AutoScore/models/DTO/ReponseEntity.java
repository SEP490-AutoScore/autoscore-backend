package com.CodeEvalCrew.AutoScore.models.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReponseEntity<T> {
    private String message;
    private T data;
    private boolean isSucess;

    public void ok() {
        this.isSucess = true;
    }

    public void ok(T data) {
        this.isSucess = true;
        this.data = data;
    }

    public void error(String message){
        this.isSucess = false;
        this.message = message;
    }
}
