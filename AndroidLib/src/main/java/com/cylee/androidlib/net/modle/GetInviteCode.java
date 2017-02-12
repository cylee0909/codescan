package com.cylee.androidlib.net.modle;

import com.cylee.androidlib.net.Config;
import com.cylee.androidlib.net.InputBase;

import java.io.Serializable;


/*
 * 
 * Generator Created
 */

public class GetInviteCode implements Serializable {

    public static class Input extends InputBase {
        Input() {
            url = "/napi/user/getinvitecode";
        }
    }

    public static Input buildInput() {
        return new Input();
    }

    /**
     *
     */
    public int invitationCode = 0;
}
