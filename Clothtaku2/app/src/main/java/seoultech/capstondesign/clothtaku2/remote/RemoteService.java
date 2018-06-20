package seoultech.capstondesign.clothtaku2.remote;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import seoultech.capstondesign.clothtaku2.item.ClothInfoItem;
import seoultech.capstondesign.clothtaku2.item.KeepItem;
import seoultech.capstondesign.clothtaku2.item.MemberInfoItem;

/**
 * 서버에 호출할 메소드를 선언하는 인터페이스
 */
public interface RemoteService {
    //내부ip와 Default포트 번호인 3000번 포트로 연결을 한다.
    String BASE_URL = "http://192.168.1.98:3000";
    String MEMBER_ICON_URL = BASE_URL + "/member/";
    String IMAGE_URL = BASE_URL + "/img/";

    //사용자 정보
    @GET("/member/{phone}")
    Call<MemberInfoItem> selectMemberInfo(@Path("phone") String phone);

    @POST("/member/info")
    Call<String> insertMemberInfo(@Body MemberInfoItem memberInfoItem);

    @FormUrlEncoded
    @POST("/member/phone")
    Call<String> insertMemberPhone(@Field("phone") String phone);

    @Multipart
    @POST("/member/icon_upload")
    Call<ResponseBody> uploadMemberIcon(@Part("member_seq") RequestBody memberSeq,
                                        @Part MultipartBody.Part file);

    //상점 정보
    @GET("/cloth/info/{info_seq}")
    Call<ClothInfoItem> selectClothInfo(@Path("info_seq") int clothInfoSeq,
                                       @Query("member_seq") int memberSeq);

    @POST("/cloth/info")
    Call<String> insertClothInfo(@Body ClothInfoItem infoItem);

    @Multipart
    @POST("/cloth/info/image")
    Call<ResponseBody> uploadClothImage(@Part("info_seq") RequestBody infoSeq,
                                       @Part("image_memo") RequestBody imageMemo,
                                       @Part MultipartBody.Part file);

    @GET("/cloth/list")
    Call<ArrayList<ClothInfoItem>> listClothInfo(@Query("member_seq") int memberSeq,
                                               @Query("user_latitude") double userLatitude,
                                               @Query("user_longitude") double userLongitude,
                                               @Query("order_type") String orderType,
                                               @Query("current_page") int currentPage);


    //지도
    @GET("/cloth/map/list")
    Call<ArrayList<ClothInfoItem>> listMap(@Query("member_seq") int memberSeq,
                                          @Query("latitude") double latitude,
                                          @Query("longitude") double longitude,
                                          @Query("distance") int distance,
                                          @Query("user_latitude") double userLatitude,
                                          @Query("user_longitude") double userLongitude);


    //즐겨찾기
    @POST("/keep/{member_seq}/{info_seq}")
    Call<String> insertKeep(@Path("member_seq") int memberSeq, @Path("info_seq") int infoSeq);

    @DELETE("/keep/{member_seq}/{info_seq}")
    Call<String> deleteKeep(@Path("member_seq") int memberSeq, @Path("info_seq") int infoSeq);

    @GET("/keep/list")
    Call<ArrayList<KeepItem>> listKeep(@Query("member_seq") int memberSeq,
                                       @Query("user_latitude") double userLatitude,
                                       @Query("user_longitude") double userLongitude);
}