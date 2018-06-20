package seoultech.capstondesign.clothtaku2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import seoultech.capstondesign.clothtaku2.adapter.InfoListAdapter;
import seoultech.capstondesign.clothtaku2.custom.EndlessRecyclerViewScrollListener;
import seoultech.capstondesign.clothtaku2.item.ClothInfoItem;
import seoultech.capstondesign.clothtaku2.item.GeoItem;
import seoultech.capstondesign.clothtaku2.lib.MyLog;
import seoultech.capstondesign.clothtaku2.remote.RemoteService;
import seoultech.capstondesign.clothtaku2.remote.ServiceGenerator;

/**
 * 상점 정보 리스트를 보여주는 프래그먼트
 */
public class ClothtakuListFragment extends Fragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();


    Context context;

    int memberSeq;

    RecyclerView ClothList;
    TextView noDataText;

    TextView orderMeter;
    TextView orderFavorite;
    TextView orderRecent;

    ImageView listType;

    InfoListAdapter infoListAdapter;
    StaggeredGridLayoutManager layoutManager;
    EndlessRecyclerViewScrollListener scrollListener;

    int listTypeValue = 2;
    String orderType;

    /**
     * ClothtakuListFragment 인스턴스를 생성한다.
     * @return BestFoodListFragment 인스턴스
     */
    public static ClothtakuListFragment newInstance() {
        ClothtakuListFragment f = new ClothtakuListFragment();
        return f;
    }

    /**
     * fragment_clothtaku_listt.xml 기반으로 뷰를 생성한다.
     * @param inflater XML를 객체로 변환하는 LayoutInflater 객체
     * @param container null이 아니라면 부모 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     * @return 생성한 뷰 객체
     */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();

        memberSeq = ((MyApp)this.getActivity().getApplication()).getMemberSeq();

        View layout = inflater.inflate(R.layout.fragment_clothtaku_list, container, false);

        return layout;
    }

    /**
     * 프래그먼트가 일시 중지 상태가 되었다가 다시 보여질 때 호출된다.
     * ClothtakuInfoActivity가 실행된 후,
     * 즐겨찾기 상태가 변경되었을 경우 이를 반영하는 용도로 사용한다.
     */
    @Override
    public void onResume() {
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        ClothInfoItem currentInfoItem = myApp.getClothInfoItem();

        if (infoListAdapter != null && currentInfoItem != null) {
            infoListAdapter.setItem(currentInfoItem);
            myApp.setClothInfoItem(null);
        }
    }

    /**
     * onCreateView() 메소드 뒤에 호출되며 화면 뷰들을 설정한다.
     * @param view onCreateView() 메소드에 의해 반환된 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_list);

        orderType = seoultech.capstondesign.clothtaku2.Constant.ORDER_TYPE_METER;

        ClothList = (RecyclerView) view.findViewById(R.id.list);
        noDataText = (TextView) view.findViewById(R.id.no_data);
        listType = (ImageView) view.findViewById(R.id.list_type);

        orderMeter = (TextView) view.findViewById(R.id.order_meter);
        orderFavorite = (TextView) view.findViewById(R.id.order_favorite);
        orderRecent = (TextView) view.findViewById(R.id.order_recent);

        orderMeter.setOnClickListener(this);
        orderFavorite.setOnClickListener(this);
        orderRecent.setOnClickListener(this);
        listType.setOnClickListener(this);

        setRecyclerView();

        listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, 0);
    }


    /**
     * 상점 정보를 스태거드그리드레이아웃으로 보여주도록 설정한다.
     * @param row 스태거드그리드레이아웃에 사용할 열의 개수
     */
    private void setLayoutManager(int row) {
        layoutManager = new StaggeredGridLayoutManager(row, StaggeredGridLayoutManager.VERTICAL);
        layoutManager
                .setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        ClothList.setLayoutManager(layoutManager);
    }

    /**
     * 리사이클러뷰를 설정하고 스크롤 리스너를 추가한다.
     */
    private void setRecyclerView() {
        setLayoutManager(listTypeValue);

        infoListAdapter = new InfoListAdapter(context,
                R.layout.row_clothtaku_list, new ArrayList<ClothInfoItem>());
        ClothList.setAdapter(infoListAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, page);
            }
        };
        ClothList.addOnScrollListener(scrollListener);
    }

    /**
     * 서버에서 상점 정보를 조회한다.
     * @param memberSeq 사용자 시퀀스
     * @param userLatLng 사용자 위도 경도 객체
     * @param orderType 맛집 정보 정렬 순서
     * @param currentPage 현재 페이지
     */
    private void listInfo(int memberSeq, LatLng userLatLng, String orderType, final int currentPage) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<ClothInfoItem>> call = remoteService.listClothInfo(memberSeq, userLatLng.latitude,
                                                userLatLng.longitude, orderType, currentPage);
        call.enqueue(new Callback<ArrayList<ClothInfoItem>>() {
            @Override
            public void onResponse(Call<ArrayList<ClothInfoItem>> call,
                                   Response<ArrayList<ClothInfoItem>> response) {
                ArrayList<ClothInfoItem> list = response.body();

                if (response.isSuccessful() && list != null) {
                    infoListAdapter.addItemList(list);

                    if (infoListAdapter.getItemCount() == 0) {
                        noDataText.setVisibility(View.VISIBLE);
                    } else {
                        noDataText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ClothInfoItem>> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    /**
     * 각종 버튼에 대한 클릭 처리를 정의한다.
     * @param v 클릭한 뷰에 대한 정보
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.list_type) {
            changeListType();

        } else {
            if (v.getId() == R.id.order_meter) {
                orderType = seoultech.capstondesign.clothtaku2.Constant.ORDER_TYPE_METER;

                setOrderTextColor(R.color.text_color_green,
                        R.color.text_color_black, R.color.text_color_black);

            } else if (v.getId() == R.id.order_favorite) {
                orderType = seoultech.capstondesign.clothtaku2.Constant.ORDER_TYPE_FAVORITE;

                setOrderTextColor(R.color.text_color_black,
                        R.color.text_color_green, R.color.text_color_black);

            } else if (v.getId() == R.id.order_recent) {
                orderType = seoultech.capstondesign.clothtaku2.Constant.ORDER_TYPE_RECENT;

                setOrderTextColor(R.color.text_color_black,
                        R.color.text_color_black, R.color.text_color_green);
            }

            setRecyclerView();
            listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, 0);
        }
    }

    /**
     * 상점 정보 정렬 방식의 텍스트 색상을 설정한다.
     * @param color1 거리순 색상
     * @param color2 인기순 색상
     * @param color3 최근순 색상
     */
    private void setOrderTextColor(int color1, int color2, int color3) {
        orderMeter.setTextColor(ContextCompat.getColor(context, color1));
        orderFavorite.setTextColor(ContextCompat.getColor(context, color2));
        orderRecent.setTextColor(ContextCompat.getColor(context, color3));
    }

    /**
     * 리사이클러뷰의 리스트 형태를 변경한다.
     */
    private void changeListType() {
        if (listTypeValue == 1) {
            listTypeValue = 2;
            listType.setImageResource(R.drawable.ic_list2);
        } else {
            listTypeValue = 1;
            listType.setImageResource(R.drawable.ic_list);

        }
        setLayoutManager(listTypeValue);
    }
}
