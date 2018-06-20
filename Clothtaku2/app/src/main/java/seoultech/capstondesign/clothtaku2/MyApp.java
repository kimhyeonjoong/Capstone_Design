package seoultech.capstondesign.clothtaku2;

import android.app.Application;
import android.os.StrictMode;


/**
 * Created by hyeonjung on 2017. 10. 10..
 */

public class MyApp extends Application {
    private seoultech.capstondesign.clothtaku2.item.MemberInfoItem memberInfoItem;
    private seoultech.capstondesign.clothtaku2.item.ClothInfoItem clothInfoItem;

    @Override
    public void onCreate() {
        super.onCreate();

        // FileUriExposedException 문제를 해결하기 위한 코드
        // 관련 설명은 책의 [참고] 페이지 참고
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public seoultech.capstondesign.clothtaku2.item.MemberInfoItem getMemberInfoItem() {
        if (memberInfoItem == null) memberInfoItem = new seoultech.capstondesign.clothtaku2.item.MemberInfoItem();

        return memberInfoItem;
    }

    public void setMemberInfoItem(seoultech.capstondesign.clothtaku2.item.MemberInfoItem item) {
        this.memberInfoItem = item;
    }

    public int getMemberSeq() {
        return memberInfoItem.seq;
    }

    public void setClothInfoItem(seoultech.capstondesign.clothtaku2.item.ClothInfoItem clothInfoItem) {
        this.clothInfoItem = clothInfoItem;
    }

    public seoultech.capstondesign.clothtaku2.item.ClothInfoItem getClothInfoItem() {
        return clothInfoItem;
    }
}
