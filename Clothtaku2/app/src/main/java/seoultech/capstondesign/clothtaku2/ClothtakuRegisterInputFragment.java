package seoultech.capstondesign.clothtaku2;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.parceler.Parcels;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import seoultech.capstondesign.clothtaku2.item.ClothInfoItem;
import seoultech.capstondesign.clothtaku2.lib.EtcLib;
import seoultech.capstondesign.clothtaku2.lib.GeoLib;
import seoultech.capstondesign.clothtaku2.lib.GoLib;
import seoultech.capstondesign.clothtaku2.lib.MyLog;
import seoultech.capstondesign.clothtaku2.lib.MyToast;
import seoultech.capstondesign.clothtaku2.lib.StringLib;
import seoultech.capstondesign.clothtaku2.remote.RemoteService;
import seoultech.capstondesign.clothtaku2.remote.ServiceGenerator;

/**
 * 상점 정보를 입력하는 액티비티
 */
public class ClothtakuRegisterInputFragment extends Fragment implements View.OnClickListener {
    public static final String INFO_ITEM = "INFO_ITEM";
    private final String TAG = this.getClass().getSimpleName();

    Context context;
    ClothInfoItem infoItem;
    Address address;

    EditText nameEdit;
    EditText telEdit;
    EditText descriptionEdit;
    TextView currentLength;

    /**
     * ClothInfoItem 객체를 인자로 저장하는
     * ClothtakuRegisterInputFragment 인스턴스를 생성해서 반환한다.
     * @param infoItem 상점 정보를 저장하는 객체
     * @return ClothtakuRegisterInputFragment 인스턴스
     */
    public static ClothtakuRegisterInputFragment newInstance(ClothInfoItem infoItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INFO_ITEM, Parcels.wrap(infoItem));

        ClothtakuRegisterInputFragment fragment = new ClothtakuRegisterInputFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * 프래그먼트가 생성될 때 호출되며 인자에 저장된 ClothInfoItem를
     * ClothtakuRegisterActivity에 currentItem를 저장한다.
     * @param savedInstanceState 프래그먼트가 새로 생성되었을 경우, 이전 상태 값을 가지는 객체
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            infoItem = Parcels.unwrap(getArguments().getParcelable(INFO_ITEM));
            if (infoItem.seq != 0) {
                ClothtakuRegisterActivity.currentItem = infoItem;
            }
            MyLog.d(TAG, "infoItem " + infoItem);
        }
    }

    /**
     * fragment_clothtaku_register_input.xmll 기반으로 뷰를 생성한다.
     * @param inflater XML를 객체로 변환하는 LayoutInflater 객체
     * @param container null이 아니라면 부모 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     * @return 생성한 뷰 객체
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        address = GeoLib.getInstance().getAddressString(context,
                       new LatLng(infoItem.latitude, infoItem.longitude));
        MyLog.d(TAG, "address" + address);

        return inflater.inflate(R.layout.fragment_clothtaku_register_input, container, false);
    }

    /**
     * onCreateView() 메소드 뒤에 호출되며 맛집 정보를 입력할 뷰들을 생성한다.
     * @param view onCreateView() 메소드에 의해 반환된 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentLength = (TextView) view.findViewById(R.id.current_length);
        nameEdit = (EditText) view.findViewById(R.id.clothtaku_name);
        telEdit = (EditText) view.findViewById(R.id.clothtaku_tel);
        descriptionEdit = (EditText) view.findViewById(R.id.clothtaku_description);
        descriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentLength.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        EditText addressEdit = (EditText) view.findViewById(R.id.clothtaku_address);

        infoItem.address = GeoLib.getInstance().getAddressString(address);
        if (!StringLib.getInstance().isBlank(infoItem.address)) {
            addressEdit.setText(infoItem.address);
        }

        Button prevButton = (Button) view.findViewById(R.id.prev);
        prevButton.setOnClickListener(this);

        Button nextButton = (Button) view.findViewById(R.id.next);
        nextButton.setOnClickListener(this);
    }

    /**
     * 클릭이벤트를 처리한다.
     * @param v 클릭한 뷰에 대한 정보
     */
    @Override
    public void onClick(View v) {
        infoItem.name = nameEdit.getText().toString();
        infoItem.tel = telEdit.getText().toString();
        infoItem.description = descriptionEdit.getText().toString();
        MyLog.d(TAG, "onClick imageItem " + infoItem);

        if (v.getId() == R.id.prev) {
            GoLib.getInstance().goFragment(getFragmentManager(),
                    R.id.content_main, seoultech.capstondesign.clothtaku2.ClothtakuRegisterLocationFragment.newInstance(infoItem));
        } else if (v.getId() == R.id.next) {
            save();
        }
    }

    /**
     * 사용자가 입력한 정보를 확인하고 저장한다.
     */
    private void save() {
        if (StringLib.getInstance().isBlank(infoItem.name)) {
            MyToast.s(context, context.getResources().getString(R.string.input_clothtaku_name));
            return;
        }

        if (StringLib.getInstance().isBlank(infoItem.tel)
                || !EtcLib.getInstance().isValidPhoneNumber(infoItem.tel)) {
            MyToast.s(context, context.getResources().getString(R.string.not_valid_tel_number));
            return;
        }

        insertClothInfo();
    }

    /**
     * 사용자가 입력한 정보를 서버에 저장한다.
     */
    private void insertClothInfo() {
        MyLog.d(TAG, infoItem.toString());

        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertClothInfo(infoItem);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    int seq = 0;
                    String seqString = response.body();

                    try {
                        seq = Integer.parseInt(seqString);
                    } catch (Exception e) {
                        seq = 0;
                    }

                    if (seq == 0) {
                        //등록 실패
                    } else {
                        infoItem.seq = seq;
                        goNextPage();
                    }
                } else { // 등록 실패
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    MyLog.d(TAG, "fail " + statusCode + errorBody.toString());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
            }
        });
    }

    /**
     * 상점 이미지를 등록할 수 있는 프래그먼트로 이동한다.
     */
    private void goNextPage() {
        GoLib.getInstance().goFragmentBack(getFragmentManager(),
                R.id.content_main, ClothtakuRegisterImageFragment.newInstance(infoItem.seq));
    }
}
