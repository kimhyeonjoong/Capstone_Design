package seoultech.capstondesign.clothtaku2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import seoultech.capstondesign.clothtaku2.adapter.MapListAdapter;
import seoultech.capstondesign.clothtaku2.item.ClothInfoItem;
import seoultech.capstondesign.clothtaku2.item.GeoItem;
import seoultech.capstondesign.clothtaku2.lib.GeoLib;
import seoultech.capstondesign.clothtaku2.lib.GoLib;
import seoultech.capstondesign.clothtaku2.lib.MyLog;
import seoultech.capstondesign.clothtaku2.lib.MyToast;
import seoultech.capstondesign.clothtaku2.remote.RemoteService;
import seoultech.capstondesign.clothtaku2.remote.ServiceGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 지도에서 상점 위치를 보여주는 프래그먼트
 */
public class ClothtakuMapFragment extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
                     GoogleMap.OnMapClickListener, GoogleMap.OnCameraMoveListener {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    int memberSeq;
    GoogleMap map;

    LatLng currentLatLng;
    int distanceMeter = 640;
    int currentZoomLevel = seoultech.capstondesign.clothtaku2.Constant.MAP_ZOOM_LEVEL_DETAIL;
    boolean isOnList = false;

    Toast zoomGuideToast;

    private HashMap<Marker, ClothInfoItem> markerMap = new HashMap<>();

    RecyclerView list;
    MapListAdapter adapter;
    ArrayList<ClothInfoItem> infoList = new ArrayList<>();

    Button listOpen;

    /**
     * ClothtakuMapFragment 인스턴스를 생성해서 반환한다.
     * @return ClothtakuMapFragment 인스턴스
     */
    public static ClothtakuMapFragment newInstance() {
        ClothtakuMapFragment f = new ClothtakuMapFragment();
        return f;
    }

    /**
     * fragment_clothtaku_map.xmll 기반으로 뷰를 생성한다.
     * @param inflater XML를 객체로 변환하는 LayoutInflater 객체
     * @param container null이 아니라면 부모 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     * @return 생성한 뷰 객체
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();

        memberSeq = ((MyApp)this.getActivity().getApplication()).getMemberSeq();

        View v = inflater.inflate(R.layout.fragment_clothtaku_map, container, false);

        return v;
    }

    /**
     * onCreateView() 메소드 뒤에 호출되며 구글맵을 화면에 보여준다.
     * 그리고 화면 구성을 위한 작업을 한다.
     * @param view onCreateView() 메소드에 의해 반환된 뷰
     * @param savedInstanceState null이 아니라면 이전에 저장된 상태를 가진 객체
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_map);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
        fragment.getMapAsync(this);

        list = (RecyclerView) view.findViewById(R.id.list);
        adapter = new MapListAdapter(context, R.layout.row_clothtaku_map, infoList);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);

        listOpen = (Button) view.findViewById(R.id.list_open);
        listOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() == 0) {
                    MyToast.s(context, context.getResources().getString(R.string.no_list));
                    return;
                }
                setInfoList(!isOnList);
            }
        });
    }

    /**
     * 오른쪽 상단에 상점 목록을 보여주는 버튼을 현재 상태 on 인자에 기반하여 설정한다.
     * @param on 현재 버튼 상태가 목록보기라면 true, 목록닫기라면 false
     */
    private void setInfoList(boolean on) {
        if (!on) {
            isOnList = false;
            list.setVisibility(View.GONE);
            listOpen.setText(R.string.list_open);
        } else {
            isOnList = true;
            list.setVisibility(View.VISIBLE);
            listOpen.setText(R.string.list_close);
        }
    }

    /**
     * 구글맵이 준비되었을 때 호출되며 구글맵을 설정하고 기본 마커를 추가하는 작업을 한다.
     * @param map 구글맵 객체
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        map.setInfoWindowAdapter(null);

        map.setOnMarkerClickListener(this);

        String fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;

        if (ActivityCompat.checkSelfPermission(context, fineLocationPermission)
                != PackageManager.PERMISSION_GRANTED) return;

        map.setMyLocationEnabled(true);

        map.setOnMapClickListener(this);
        map.setOnCameraMoveListener(this);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);
        setting.setMapToolbarEnabled(false);

        MyLog.d(TAG, GeoItem.getKnownLocation().toString());

        if (GeoItem.getKnownLocation() != null) {
            movePosition(GeoItem.getKnownLocation(), seoultech.capstondesign.clothtaku2.Constant.MAP_ZOOM_LEVEL_DETAIL);
        }
        showList();
    }

    /**
     * 구글맵에서 마커가 클릭되었을 때 호출된다.
     * @param marker 클릭한 마커에 대한 정보를 가진 객체
     * @return 마커 이벤트를 처리했다면 true, 그렇지 않다면 false
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        ClothInfoItem item = markerMap.get(marker);
        GoLib.getInstance().goClothtakuInfoActivity(context, item.seq);
        return true;
    }

    /**
     * 구글맵의 카메라를 위도와 경도 그리고 줌레벨을 기반으로 이동한다.
     * @param latlng 위도, 경도 객체
     * @param zoomLevel 줌레벨
     */
    private void movePosition(LatLng latlng, float zoomLevel) {
        CameraPosition cp = new CameraPosition.Builder().target((latlng)).zoom(zoomLevel).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    /**
     * 사용자가 맵을 클릭했을 때 하단 버튼이 목록보기 상태라면 목록닫기 상태로 변경한다.
     * @param latLng 위도, 경도 객체
     */
    @Override
    public void onMapClick(LatLng latLng) {
        setInfoList(false);
    }

    /**
     * 주어진 정보를 기반으로 상점 정보를 조회하고 지도에 표시한다.
     * @param memberSeq 사용자 시퀀스
     * @param latLng 위도, 경도 객체
     * @param distance 거리
     * @param userLatLng 사용자 현재 위도, 경도 객체
     */
    private void listMap(int memberSeq, LatLng latLng, int distance, LatLng userLatLng) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<ClothInfoItem>> call = remoteService.listMap(memberSeq, latLng.latitude,
                latLng.longitude, distance, userLatLng.latitude, userLatLng.longitude);
        call.enqueue(new Callback<ArrayList<ClothInfoItem>>() {
            @Override
            public void onResponse(Call<ArrayList<ClothInfoItem>> call,
                                   Response<ArrayList<ClothInfoItem>> response) {
                ArrayList<ClothInfoItem> list = response.body();

                if (list == null) {
                    list = new ArrayList<>();
                }

                if (response.isSuccessful()) {
                    setMap(list);
                    infoList = list;
                } else {
                    MyLog.d(TAG, "not success");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ClothInfoItem>> call, Throwable t) {
            }
        });
    }

    /**
     * 상점 리스트를 지도에 표시하는 메소드를 호출하고 지도에서 원을 그린다.
     * @param list 상점 리스트
     */
    private void setMap(ArrayList<ClothInfoItem> list) {
        if (map != null && list != null) {
            map.clear();
            addMarker(list);
        }
        adapter.setItemList(list);
        drawCircle(currentLatLng);
    }

    /**
     * 상점 리스트를 지도에 추가한다.
     * @param list 상점 리스트
     */

    private void addMarker(ArrayList<ClothInfoItem> list) {
        MyLog.d(TAG, "addMarker list.size() " + list.size());

        if (list == null || list.size() == 0) return;

        for (ClothInfoItem item : list) {
            MyLog.d(TAG, "addMarker " + item);
            if (item.latitude != 0 && item.longitude != 0) {
                Marker marker = map.addMarker(getMarker(item));

                markerMap.put(marker, item);
            }
        }
    }

    /**
     * ClothInfoItem으로 지도에 표시할 마커를 생성한다.
     * @param item 상점 정보 아이템 객체
     * @return 지도에 표시할 마커 객체
     */
    private MarkerOptions getMarker(ClothInfoItem item) {
        final MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(item.latitude, item.longitude));
        marker.title(item.name);
        marker.snippet(item.tel);
        marker.draggable(false);

        return marker;
    }

    /**
     * 상점 마커를 표시할 수 있는 원을 지도에 그린다.
     * @param position 위도, 경도 객체
     */
    private void drawCircle(LatLng position) {
        double radiusInMeters = distanceMeter;
        int strokeColor = 0x440000ff;
        int shadeColor = 0x110000ff;

        CircleOptions circleOptions
                = new CircleOptions().center(position).radius(radiusInMeters)
                      .fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(4);
        map.addCircle(circleOptions);
    }

    /**
     * 지도를 움직일 경우 상점 정보를 조회해서 화면에 표시할 수 있도록 한다.
     */
    @Override
    public void onCameraMove() {
        showList();
    }

    /**
     * 지도를 일정 레벨 이상 확대했을 경우, 해당 위치에 있는 상점 리스트를 서버에 요청한다.
     */
    private void showList() {
        currentZoomLevel = (int) map.getCameraPosition().zoom;
        currentLatLng = map.getCameraPosition().target;

        if (currentZoomLevel < seoultech.capstondesign.clothtaku2.Constant.MAP_MAX_ZOOM_LEVEL) {

            map.clear();

            if (zoomGuideToast != null) {
                zoomGuideToast.cancel();
            }
            zoomGuideToast = Toast.makeText(context
                                , getResources().getString(R.string.message_zoom_level_max_over)
                                , Toast.LENGTH_SHORT);
            zoomGuideToast.show();

            return;
        }

        distanceMeter = GeoLib.getInstance().getDistanceMeterFromScreenCenter(map);

        listMap(memberSeq, currentLatLng, distanceMeter, GeoItem.getKnownLocation());
    }
}