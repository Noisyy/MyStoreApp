package com.example.quantrasuaclient.Remote;


import com.example.quantrasuaclient.Model.FCMResponse;
import com.example.quantrasuaclient.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    //Call API Firebase Cloud Messaging
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAAnAHFPY:APA91bE4e_IlZQQuH8juhBsnVOT931AGZE77PZyQBPyxENWdlIOVLrk_eqL2Hzy16PDFQ5-TaJ_k16rmAK6pVi3Ct4mcoXr2zRvcSfQYbJeZ-aYvIfBEm5Xcz3DsLzKnWBizXnl86cHx"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
