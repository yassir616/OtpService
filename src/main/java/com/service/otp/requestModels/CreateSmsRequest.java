package com.service.otp.requestModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CreateSmsRequest {
    private String sms_to;
    private String sms_text;
    private String sms_from;
    private int sms_domaine_id;
    private int sms_reason_id;
    private String sms_complement;
    private int sms_complement_id;

}
