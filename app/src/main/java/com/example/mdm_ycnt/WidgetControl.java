package com.example.mdm_ycnt;

import static com.example.mdm_ycnt.UniversalFunction.F_md5;
import static com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule.MOVE_AXIS_X;
import static com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule.MOVE_AXIS_XY;
import static com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule.MOVE_AXIS_Y;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ycnt.imds.floatingwindow.FloatingLayout;
import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class WidgetControl {

    private final Context mContext;

    Map<String, List<DailyEvent>> schedule = null;

    public WidgetControl(Context mContext){

        this.mContext = mContext;
        CreateWidget();

    }

    // AQI
    private void setAQI(View view){

        upDateAQI(view);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                upDateAQI(view);
            }
        }, 0, 10 * 1000);

    }

    private void upDateAQI(View view){

        JSONObject postJson = new JSONObject();
        String phpUrl = "https://imds.tw/mdm/php/api/getAQI.php";

        TextView textAQINumber = view.findViewById(R.id.textAQINumber);
        TextView textSiteName = view.findViewById(R.id.textSiteName);
        TextView textUpdateTime = view.findViewById(R.id.textUpdateTime);

        ImageView imageViewAQI = view.findViewById(R.id.imageViewAQI);
        imageViewAQI.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    JSONObject getJson = new UniversalFunction().httpPostData(postJson, phpUrl);
                    JSONObject aqiInfo = getJson.getJSONObject("aqiInfo");

                    String county = aqiInfo.getString("county");
                    String siteName = aqiInfo.getString("sitename");
                    String aqi = aqiInfo.getString("aqi");

                    String[] publishTime = aqiInfo.getString("publishtime").split(":");

                    textAQINumber.setText(aqi);
                    textAQINumber.setTextColor(Color.parseColor(getAQIColor(Integer.parseInt(aqi))));

                    textSiteName.setText(county + "-" +siteName);
                    textUpdateTime.setText(publishTime[0] + ":" + publishTime[1]);

                    /////////////////////////////////////////

                    String getAqiPhotoUrl = getAQIPhotoUrl(Integer.parseInt(aqi));

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext)
                                    .load(getAqiPhotoUrl)
                                    .into(imageViewAQI);
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private String getAQIColor(int aqiValue){

//        良好：5ad390
//        不良：ffe700
//        對敏感族群不良：ff9d00
//        對所有族群不良：ff4a00
//        非常不良：700066
//        有害：840008

        if(aqiValue >= 0 && aqiValue <= 50){
            return "#5ad390";
        }else if(aqiValue >= 51 && aqiValue <= 100){
            return "#ffe700";
        }else if(aqiValue >= 101 && aqiValue <= 150){
            return "#ff9d00";
        }else if(aqiValue >= 151 && aqiValue <= 200){
            return "#ff4a00";
        }else if(aqiValue >= 201 && aqiValue <= 300){
            return "#700066";
        }else if(aqiValue >= 301 && aqiValue <= 500){
            return "#840008";
        }

        return "#757575";
    }

    private String getAQIPhotoUrl(int aqiValue){

        String folderPath = "https://imds.tw/mdm/media/AqiImg/";
        String fileName = null;

        if(aqiValue >= 0 && aqiValue <= 50){
            fileName = "0-50.png";
        }else if(aqiValue >= 51 && aqiValue <= 100){
            fileName = "51-100.png";
        }else if(aqiValue >= 101 && aqiValue <= 150){
            fileName = "101-150.png";
        }else if(aqiValue >= 151 && aqiValue <= 200){
            fileName = "151-200.png";
        }else if(aqiValue >= 201 && aqiValue <= 300){
            fileName = "201-300.png";
        }else if(aqiValue >= 301 && aqiValue <= 500){
            fileName = "301-500.png";
        }

        return folderPath + fileName;

    }

    // 時鐘
    private void setClock(View view){

        TextView textNowTime = view.findViewById(R.id.textNowTime);
        TextView textTodayDate = view.findViewById(R.id.textTodayDate);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                textTodayDate.setText(currentDate);
                textNowTime.setText(currentTime);

            }
        }, 0, 1000);

    }

    // 行事曆
    private void setCalenderView(View view){

        CalendarView calendarView = view.findViewById(R.id.calendarView);

        ViewGroup viewGroupCalendarList = view.findViewById(R.id.viewGroupCalendarList);
        View buttonToday = view.findViewById(R.id.buttonToday);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        getCalenderData().thenAccept(getData -> {

            schedule = getData;

            // 測試用
//                Calendar specificDate = Calendar.getInstance();
//                specificDate.set(2024, 5, 11);
//                long specificDateInMillis = specificDate.getTimeInMillis();
//                calendarView.setDate(specificDateInMillis, true, false); // 設定為指定時間
//                appendCalendar(viewGroupCalendarList, getScheduleOfDay(schedule, 2024, 5, 11));
            //以上--------

            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {

                    List<DailyEvent> eventList = getScheduleOfDay(schedule, year, month, dayOfMonth);
                    appendCalendar(viewGroupCalendarList, eventList);

                }
            });

            buttonToday.setOnClickListener(v -> {

                getCalenderData().thenAccept(getSchedule -> {

                    schedule = getSchedule;

                });

                Calendar today = Calendar.getInstance();
                int year = today.get(Calendar.YEAR);
                int month = today.get(Calendar.MONTH); // 月份從0開始
                int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

                List<DailyEvent> eventList = getScheduleOfDay(schedule, year, month, dayOfMonth);
                appendCalendar(viewGroupCalendarList, eventList);

                Calendar specificDateToday = Calendar.getInstance();
                specificDateToday.set(year, month, dayOfMonth);
                long specificDateTodayInMillis = specificDateToday.getTimeInMillis();
                calendarView.setDate(specificDateTodayInMillis, true, false); // 設定為指定時間

            });

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    buttonToday.performClick();
                }
            });


        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                JSONObject calendarInfo = getCalendarInfo();
//                JSONArray calendarItems = calendarInfo.optJSONArray("calendar_items");
//
//                List<Event> events = new ArrayList<>();
//
//                for (int i = 0; i < calendarItems.length(); i++) {
//
//                    JSONObject getCalendarInfo = calendarItems.optJSONObject(i);
//
//                    String title = getCalendarInfo.optString("title");
//                    String content = getCalendarInfo.optString("content");
//                    String startDate = getCalendarInfo.optString("start_date");
//                    String startTime = getCalendarInfo.optString("start_time");
//                    String endDate = getCalendarInfo.optString("end_date");
//                    String endTime = getCalendarInfo.optString("end_time");
//                    String location = getCalendarInfo.optString("location");
//
//                    events.add(new Event(title, content, startDate, startTime, endDate, endTime, location));
//                }
//
//                Map<String, List<DailyEvent>> schedule = organizeSchedule(events);
//
//                CalendarView calendarView = view.findViewById(R.id.calendarView);
//
//                ViewGroup viewGroupCalendarList = view.findViewById(R.id.viewGroupCalendarList);
//                View buttonToday = view.findViewById(R.id.buttonToday);
//
//                // 測試用
////                Calendar specificDate = Calendar.getInstance();
////                specificDate.set(2024, 5, 11);
////                long specificDateInMillis = specificDate.getTimeInMillis();
////                calendarView.setDate(specificDateInMillis, true, false); // 設定為指定時間
////                appendCalendar(viewGroupCalendarList, getScheduleOfDay(schedule, 2024, 5, 11));
//                //以上--------
//
//                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//                    @Override
//                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
//
//                        List<DailyEvent> eventList = getScheduleOfDay(schedule, year, month, dayOfMonth);
//                        appendCalendar(viewGroupCalendarList, eventList);
//
//                    }
//                });
//
//                buttonToday.setOnClickListener(v -> {
//
//                    Calendar today = Calendar.getInstance();
//                    int year = today.get(Calendar.YEAR);
//                    int month = today.get(Calendar.MONTH); // 月份從0開始
//                    int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
//
//                    List<DailyEvent> eventList = getScheduleOfDay(schedule, year, month, dayOfMonth);
//                    appendCalendar(viewGroupCalendarList, eventList);
//
//                    Calendar specificDateToday = Calendar.getInstance();
//                    specificDateToday.set(year, month, dayOfMonth);
//                    long specificDateTodayInMillis = specificDateToday.getTimeInMillis();
//                    calendarView.setDate(specificDateTodayInMillis, true, false); // 設定為指定時間
//
//                });
//
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        buttonToday.performClick();
//                    }
//                });
//
//            }
//        }).start();

    }

    private CompletableFuture<Map<String, List<DailyEvent>>> getCalenderData(){

        CompletableFuture<Map<String, List<DailyEvent>>> future = new CompletableFuture<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject calendarInfo = getCalendarInfo();
                JSONArray calendarItems = calendarInfo.optJSONArray("calendar_items");

                List<Event> events = new ArrayList<>();

                for (int i = 0; i < calendarItems.length(); i++) {

                    JSONObject getCalendarInfo = calendarItems.optJSONObject(i);

                    String title = getCalendarInfo.optString("title");
                    String content = getCalendarInfo.optString("content");
                    String startDate = getCalendarInfo.optString("start_date");
                    String startTime = getCalendarInfo.optString("start_time");
                    String endDate = getCalendarInfo.optString("end_date");
                    String endTime = getCalendarInfo.optString("end_time");
                    String location = getCalendarInfo.optString("location");

                    events.add(new Event(title, content, startDate, startTime, endDate, endTime, location));
                }

                Map<String, List<DailyEvent>> schedule = organizeSchedule(events);

                future.complete(schedule);
            }

        }).start();


        return future;
    }

    private List<DailyEvent> getScheduleOfDay(Map<String, List<DailyEvent>> schedule, int year, int month, int dayOfMonth){

        String selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);

        List<DailyEvent> eventList = schedule.get(selectedDate);

        if (eventList != null && eventList.size() > 0){

            return eventList;

        }else {
            // 空的

            return null;

        }

    }

    private void appendCalendar(ViewGroup viewGroup, List<DailyEvent> eventList){

        viewGroup.removeAllViews();

        if(eventList != null){

            for (DailyEvent event : eventList){

                RelativeLayout layout = new RelativeLayout(mContext);
                layout.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                String title = event.event.title;
                String[] startTimeArr = event.startTime.split(":");
                String[] endTimeArr = event.endTime.split(":");

                String startTime = startTimeArr[0] + ":" + startTimeArr[1];
                String endTime = endTimeArr[0] + ":" + endTimeArr[1];

                View cardView = getCalendarCardStyle(title,  startTime, endTime);

                viewGroup.addView(cardView);

            }

        }

    }

    private JSONObject getCalendarInfo(){

        JSONObject postJson = new JSONObject();
        JSONObject calendarInfo = null;
        String phpUrl = "https://imds.tw/mdm/php/api/getGoogleCalendar.php";

        try {
            JSONObject getJson = new UniversalFunction().httpPostData(postJson, phpUrl);
            calendarInfo = getJson.getJSONObject("calendarInfo");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return calendarInfo;

    }

    private View getCalendarCardStyle(String title, String startTime, String endTime){

        CardView cardView = new CardView(mContext);
        CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );

        int marginBottom = (int) (6 * mContext.getResources().getDisplayMetrics().density);
        cardViewParams.setMargins(0, 0, 0, marginBottom);

        cardView.setLayoutParams(cardViewParams);
        cardView.setRadius(6 * mContext.getResources().getDisplayMetrics().density);
        cardView.setCardElevation(0);
        cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));


        LinearLayout outerLinearLayout = new LinearLayout(mContext);
        outerLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        outerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        int padding = (int) (8 * mContext.getResources().getDisplayMetrics().density);
        outerLinearLayout.setPadding(padding, padding / 2, padding, padding / 2);


        CardView decorateView = new CardView(mContext);
        CardView.LayoutParams decorateViewParams = new CardView.LayoutParams(
                CardView.LayoutParams.WRAP_CONTENT,
                CardView.LayoutParams.MATCH_PARENT
        );

        decorateViewParams.setMargins(0, 0, (int) (6 * mContext.getResources().getDisplayMetrics().density), 0);

        int paddingLeftRight = (int) (2 * mContext.getResources().getDisplayMetrics().density);
        decorateView.setContentPadding(paddingLeftRight, 0, paddingLeftRight, 0);

        decorateView.setLayoutParams(decorateViewParams);
        decorateView.setRadius(6 * mContext.getResources().getDisplayMetrics().density);
        decorateView.setCardElevation(0);
        decorateView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.blue));


        TextView textViewTitle = new TextView(mContext);
        LinearLayout.LayoutParams textViewTitleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textViewTitle.setLayoutParams(textViewTitleParams);
        textViewTitle.setTextSize(12);
        textViewTitle.setText(title);
        textViewTitle.setTextColor(ContextCompat.getColor(mContext, R.color.dark_blue));


        LinearLayout innerLinearLayout = new LinearLayout(mContext);
        innerLinearLayout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams innerLinearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );


        int paddingLeft = (int) (8 * mContext.getResources().getDisplayMetrics().density);
        innerLinearLayout.setPadding(paddingLeft, 0, 0, 0);

        innerLinearLayout.setLayoutParams(innerLinearLayoutParams);


        TextView textViewStartTime = new TextView(mContext);
        textViewStartTime.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textViewStartTime.setTextSize(10);
        textViewStartTime.setText(startTime);


        TextView textViewEnDTime = new TextView(mContext);
        textViewEnDTime.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textViewEnDTime.setTextSize(10);
        textViewEnDTime.setText(endTime);

        innerLinearLayout.addView(textViewStartTime);
        innerLinearLayout.addView(textViewEnDTime);

        outerLinearLayout.addView(decorateView);
        outerLinearLayout.addView(textViewTitle);
        outerLinearLayout.addView(innerLinearLayout);

        cardView.addView(outerLinearLayout);

        return cardView;
    }

    private Map<String, List<DailyEvent>> organizeSchedule(List<Event> events) {
        Map<String, List<DailyEvent>> schedule = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Event event : events) {
            LocalDate startDate = event.startDateTime.toLocalDate();
            LocalDate endDate = event.endDateTime.toLocalDate();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

                String dateString = date.format(dateFormatter);

                String startTime;
                String endTime;

                if (date.equals(startDate)) {
                    startTime = event.startDateTime.toLocalTime().format(timeFormatter);
                } else {
                    startTime = "00:00:00";
                }

                if (date.equals(endDate)) {
                    endTime = event.endDateTime.toLocalTime().format(timeFormatter);
                } else {
                    endTime = "23:59:59";
                }

                schedule.computeIfAbsent(dateString, k -> new ArrayList<>()).add(new DailyEvent(event, startTime, endTime));
            }
        }

        return schedule;
    }

    // 圖片輪播
    private void setLoopPhoto(View view){

        ImageSliderAdapter.OnItemClickListener listener = new ImageSliderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, List<String> imageUrls) {

                String data = "[";

                for (String url:imageUrls
                ) {
                    data = data + "\"" + url + "\"" + ",";
                }
                data = data.substring(0, data.length() - 1);
                data = data +"]";

                String url =
                        "https://imds.tw/mdm/tool_web/photoLoopPlayer.php?" +
                                "imgList=" + data +
                                "&num=" + position;

                createWebViewFloatingWidow(url);
            }
        };

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);

        List<String> imageUrls = Arrays.asList(
                "https://imds.tw/testPhoto/01.jpg",
                "https://imds.tw/testPhoto/02.jpg",
                "https://imds.tw/testPhoto/03.jpg"
        );



        ImageSliderAdapter adapter = new ImageSliderAdapter(mContext, imageUrls);
        adapter.setOnItemClickListener(listener);
        viewPager.setAdapter(adapter);

        // 自動輪播功能
        new AutoScroll(viewPager, adapter, 5 * 1000);


        ////////////////////////////////////////////////////////////////

        List<String> imageUrl2 = Arrays.asList(
                "https://imds.tw/testPhoto/04.jpg",
                "https://imds.tw/testPhoto/05.png"
        );


        ImageSliderAdapter adapter2 = new ImageSliderAdapter(mContext, imageUrl2);
        adapter2.setOnItemClickListener(listener);
        viewPager2.setAdapter(adapter2);

        // 自動輪播功能
        new AutoScroll(viewPager2, adapter2, 3 * 1000);

    }


    // url清單
    private void setUrlList(View view){

//        createChatGPTModel("https://chatgpt.com/");

        LinearLayout urlList = view.findViewById(R.id.urlList);

        List<Map<String, String>> urlInfoList = new ArrayList<>();

        Map<String, String> map04 = new HashMap<>();
        map04.put("title", "iMDS官網");
        map04.put("url", "https://imds.tw/");
        map04.put("icon", "https://imds.tw/testPhoto/urlIcon/imds-icon.png");
        map04.put("backgroundColor", "#4E8397");
        map04.put("textColor", "#FFFFFF");

        Map<String, String> map05 = new HashMap<>();
        map05.put("title", "iPTS官網");
        map05.put("url", "https://ipts.tw");
        map05.put("icon", "https://imds.tw/testPhoto/urlIcon/iPTS-icon.png");
        map05.put("backgroundColor", "#04b4b4");
        map05.put("textColor", "#FFFFFF");


        Map<String, String> map06 = new HashMap<>();
        map06.put("title", "教育雲");
        map06.put("url", "https://cloud.edu.tw/");
        map06.put("icon", "https://imds.tw/testPhoto/urlIcon/edu-icon.png");

        urlInfoList.add(map04);
        urlInfoList.add(map05);
        urlInfoList.add(map06);

        for (Map<String, String> getMap : urlInfoList) {

            urlList.addView(getUrlStyle(getMap));

        }

        ////////////////////////////////////////////////////////////

        LinearLayout aiUrlList = view.findViewById(R.id.aiUrlList);

        List<Map<String, String>> aiUrlInfoList = new ArrayList<>();

        Map<String, String> map01 = new HashMap<>();
        map01.put("title", "ChatGPT");
        map01.put("url", "https://chatgpt.com/");
        map01.put("icon", "https://imds.tw/testPhoto/urlIcon/ChatGPT-icon.png");
        map01.put("ClipboardInfo", "請幫我出一題 二元一次方程式的應用題目，用小明買蘋果的例子，不要提供答案");

        Map<String, String> map02 = new HashMap<>();
        map02.put("title", "Canva");
        map02.put("url", "https://www.canva.com/design/DAGHQk77njY/JYBfWBKKbbH_7PK-P4yl5w/edit?ui=eyJBIjp7IkIiOnsiQiI6dHJ1ZX19LCJFIjp7IkE_IjoiViIsIkIiOiJCIn0sIkciOnsiQiI6dHJ1ZX19");
        map02.put("icon", "https://imds.tw/testPhoto/urlIcon/Canva-icon.png");
        map02.put("info", "圖片");
        map02.put("ClipboardInfo", "一位中國古代詩人，坐在床前，窗外的月光照入室內的地上，詩人抬頭看著窗外的月亮，可愛漫畫風格");

        Map<String, String> map03 = new HashMap<>();
        map03.put("title", "Suno");
        map03.put("url", "https://suno.com/");
        map03.put("icon", "https://imds.tw/testPhoto/urlIcon/Suno-icon.png");
        map03.put("info", "音樂");
        map03.put("ClipboardInfo", "一閃一閃亮晶晶，滿天都是小星星");

        aiUrlInfoList.add(map01);
        aiUrlInfoList.add(map02);
        aiUrlInfoList.add(map03);

        for (Map<String, String> getMap : aiUrlInfoList) {

            aiUrlList.addView(getUrlStyle(getMap));

        }

    }


    private void createChatGPTModel(String url){

        int x = new UniversalFunction().dpToPx(100, mContext);
        int y = new UniversalFunction().dpToPx(25, mContext);
        int w = new UniversalFunction().dpToPx(350, mContext);
        int h = new UniversalFunction().dpToPx(650, mContext);


        FloatingLayoutConfig config =
                new FloatingLayoutConfig.Builder(mContext)
                        .setLayoutRes(-1)
                        .setMovementModule(MOVE_AXIS_XY)
                        .setGravity(Gravity.START | Gravity.TOP)
                        .setX(x)
                        .setY(y)
                        .setWidth(w)
                        .setHeight(h)
                        .setShow(false)
                        .setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
                        .build();

        FloatingLayout chatGPTFloatingWindow = new FloatingLayout(config);

        FloatingListener floatingListener = new FloatingListener() {

            @Override
            public void onCreate(View view) {

                ViewGroup viewGroup = (ViewGroup) view;

                CardView cardView = new CardView(mContext);
                CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.MATCH_PARENT
                );

                cardView.setLayoutParams(cardViewParams);
                cardView.setRadius(6 * mContext.getResources().getDisplayMetrics().density);
                cardView.setCardElevation(0);

                LinearLayout linearLayout = new LinearLayout(mContext);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.black));
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));

                RelativeLayout layout = new RelativeLayout(mContext);
//                layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue));

                RelativeLayout.LayoutParams layoutLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

                layoutLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                layout.setLayoutParams(layoutLayoutParams);

                Button closeButton = getCloseButtonStyle();

                RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                        (int) (20 * mContext.getResources().getDisplayMetrics().density),
                        (int) (20 * mContext.getResources().getDisplayMetrics().density));
                buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                // 設置偏移量，將按鈕向左偏移
                buttonParams.setMargins(
                        0,
                        (int) (5 * mContext.getResources().getDisplayMetrics().density),
                        (int) (5 * mContext.getResources().getDisplayMetrics().density),
                        (int) (5 * mContext.getResources().getDisplayMetrics().density));

                layout.addView(closeButton, buttonParams);

                linearLayout.addView(layout);


                final WebView webView = new WebView(mContext);

                webView.setBackgroundColor(Color.WHITE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                webView.getSettings().setDomStorageEnabled(true);

                webView.setFocusable(true);
                webView.setFocusableInTouchMode(true);
                webView.requestFocus(View.FOCUS_DOWN);

                WebView.setWebContentsDebuggingEnabled(true);

                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 使用緩存的方式

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        view.requestFocus(View.FOCUS_DOWN);
                    }
                });

                // Set a focus change listener to show the keyboard
                webView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {

                        if (hasFocus) {

                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    }
                });

                webView.loadUrl(url);

                linearLayout.addView(webView, new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                ));

                closeButton.setOnClickListener(v -> {

                    chatGPTFloatingWindow.hide();

                });

                cardView.addView(linearLayout);

                viewGroup.addView(cardView);

//                view.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        switch (event.getAction()) {
//
//                            case MotionEvent.ACTION_DOWN:
//
//                                return false;
//
//                            case MotionEvent.ACTION_OUTSIDE:
//
//                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                                if (imm != null) {
//                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                                }
//
//                                return false;
//                            default:
//
//                                return false;
//                        }
//                    }
//                });


                int moveAxis = 3;
                Context context = view.getContext();
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

                view.setOnTouchListener(new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;
                    private boolean isDragging;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = params.x;
                                initialY = params.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                isDragging = false; // 初始化時，沒有拖動
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                // Calculate the distance moved
                                final int dx = (int) (event.getRawX() - initialTouchX);
                                final int dy = (int) (event.getRawY() - initialTouchY);

                                // Update the position if there is significant movement
                                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                                    if (moveAxis == MOVE_AXIS_X || moveAxis == MOVE_AXIS_XY) {
                                        params.x = initialX + dx;
                                    }
                                    if (moveAxis == MOVE_AXIS_Y || moveAxis == MOVE_AXIS_XY) {
                                        params.y = initialY + dy;
                                    }
                                    windowManager.updateViewLayout(v, params);
                                    isDragging = true; // 标记为拖动
                                }
                                return true;
                            case MotionEvent.ACTION_UP:
                                if (!isDragging) {
                                    // 如果没有拖动，则触发点击事件
                                    v.performClick();
                                }
                                return true;

                            case MotionEvent.ACTION_OUTSIDE:

                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }

                                return false;

                        }
                        return false;
                    }
                });
            }

            @Override
            public void onClose() {



            }

            @Override
            public void willOpen(View view) {

            }

            @Override
            public void didOpen(View view) {

            }

            @Override
            public void willClose(View view) {

            }

            @Override
            public void didClose(View view) {

            }

        };

        chatGPTFloatingWindow.setFloatingListener(floatingListener);
        chatGPTFloatingWindow.create();

    }

    private View getUrlStyle(Map<String, String> getMap){

        String title = getMap.get("title");
        String url = getMap.get("url");
        String icon = getMap.get("icon");
        String info = getMap.get("info");

        String backgroundColor = getMap.get("backgroundColor") != null && !getMap.get("backgroundColor").equals("") ? getMap.get("backgroundColor") : "#FFFFFF";
        String textColor = getMap.get("textColor") != null && !getMap.get("textColor").equals("") ? getMap.get("textColor") : "#233239";


        CardView cardView = new CardView(mContext);
        CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );


        int marginBottom = (int) (6 * mContext.getResources().getDisplayMetrics().density);
        cardViewParams.setMargins(0, 0, 0, marginBottom);

        cardView.setLayoutParams(cardViewParams);
        cardView.setRadius(6 * mContext.getResources().getDisplayMetrics().density);
        cardView.setCardElevation(0);
        cardView.setCardBackgroundColor(Color.parseColor(backgroundColor));
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(getMap.get("ClipboardInfo") != null){

                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

                    // 創建ClipData物件
                    ClipData clip = ClipData.newPlainText("label", getMap.get("ClipboardInfo"));

                    // 將ClipData設置到剪貼簿
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }

                }

                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);

            }
        });

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams linearLayoutLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int padding = (int) (4 * mContext.getResources().getDisplayMetrics().density);
        linearLayout.setPadding(padding, padding, padding, padding);

        ImageView imageView = new ImageView(mContext);
        Glide.with(mContext)
                .load(icon)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);


        int wh = (int) (15 * mContext.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams imageViewLayoutParams = new LinearLayout.LayoutParams(
                wh,
                wh
        );
        imageViewLayoutParams.gravity = Gravity.CENTER;  // 設置居中對齊
        linearLayout.addView(imageView, imageViewLayoutParams);


        TextView textViewTitle = new TextView(mContext);
        textViewTitle.setText(Html.fromHtml("<u>"+ title +"</u>"));
        textViewTitle.setTextColor(Color.parseColor(textColor));
        textViewTitle.setPadding(padding, 0, 0, 0);

        linearLayout.addView(textViewTitle, new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        if(info != null && !info.equals("")){

            TextView textViewInfo = new TextView(mContext);
            textViewInfo.setText("("+ info +")");
            textViewInfo.setTextSize(9);
            linearLayout.addView(textViewInfo, new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ));

        }

        cardView.addView(linearLayout);

        return cardView;
    }

//    private View getUrlStyle(String title, String url){
//
//        Map<String, String> clipboardInfoMap = new HashMap<>();
//        clipboardInfoMap.put("ChatGPT", "請幫我出一題 二元一次方程式的應用題目，用小明買蘋果的例子，不要提供答案");
//        clipboardInfoMap.put("COPILOT", "創造一個ESG地球圖，周圍以各國人的手圍繞表示和平，各個手的中間再穿插樹葉，表示綠色能源");
//
//
//        CardView cardView = new CardView(mContext);
//        CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
//                CardView.LayoutParams.MATCH_PARENT,
//                CardView.LayoutParams.WRAP_CONTENT
//        );
//
//        int marginBottom = (int) (6 * mContext.getResources().getDisplayMetrics().density);
//        cardViewParams.setMargins(0, 0, 0, marginBottom);
//
//        cardView.setLayoutParams(cardViewParams);
//        cardView.setRadius(6 * mContext.getResources().getDisplayMetrics().density);
//        cardView.setCardElevation(0);
//        cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
//
//        int padding = (int) (8 * mContext.getResources().getDisplayMetrics().density);
//
//        TextView textViewTitle = new TextView(mContext);
//        textViewTitle.setText(Html.fromHtml("<u>"+ title +"</u>"));
//        textViewTitle.setTextColor(ContextCompat.getColor(mContext, R.color.dark_blue));
//        textViewTitle.setPadding(padding, padding / 2, padding, padding / 2);
//
//        textViewTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                String clipboardInfo = clipboardInfoMap.get(title);
//
//                if(clipboardInfo != null){
//
//                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//
//                    // 創建ClipData物件
//                    ClipData clip = ClipData.newPlainText("label", clipboardInfo);
//
//                    // 將ClipData設置到剪貼簿
//                    if (clipboard != null) {
//                        clipboard.setPrimaryClip(clip);
//                    }
//
//                }
//
////                createWebViewFloatingWidow(url);
//
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intent);
//
//            }
//        });
//
//        cardView.addView(textViewTitle);
//
//        return cardView;
//    }

    // 倒數日
//    private void setCountDown(View view){
//
//        TextView textCountDownTitle = view.findViewById(R.id.textCountDownTitle);
//        TextView textCountDownNum = view.findViewById(R.id.textCountDownNum);
//
//        Calendar today = Calendar.getInstance();
//        int year = today.get(Calendar.YEAR);
//        int month = today.get(Calendar.MONTH); // 月份從0開始
//        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
//
//        String toDay = year + "-" + (month + 1) + "-" + dayOfMonth;
//
//        long getDay = daysBetweenDates(toDay, "2025-05-17");
//
//        textCountDownTitle.setText("會考倒數");
//        textCountDownNum.setText(String.valueOf(getDay));
//
//    }

//    private long daysBetweenDates(String startDateStr, String endDateStr) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            Date startDate = dateFormat.parse(startDateStr);
//            Date endDate = dateFormat.parse(endDateStr);
//            long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
//            return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return -1; // Return -1 if there's an error
//        }
//    }


    private void createWebViewFloatingWidow(String url){

        FloatingLayoutConfig config =
                new FloatingLayoutConfig.Builder(mContext)
                        .setLayoutRes(-1)
                        .setMovementModule(MOVE_AXIS_XY)
                        .setGravity(Gravity.START | Gravity.TOP)
                        .setX(0)
                        .setY(0)
                        .setWidth(WindowManager.LayoutParams.MATCH_PARENT)
                        .setHeight(WindowManager.LayoutParams.MATCH_PARENT)
                        .setShow(true)
                        .setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                        .build();

        FloatingLayout floatingWindow = new FloatingLayout(config);

        FloatingListener floatingListener = new FloatingListener() {

            @Override
            public void onCreate(View view) {

                ViewGroup viewGroup = (ViewGroup) view;

                CardView cardView = new CardView(mContext);
                CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.MATCH_PARENT
                );

                cardView.setLayoutParams(cardViewParams);
                cardView.setRadius(6 * mContext.getResources().getDisplayMetrics().density);
                cardView.setCardElevation(0);

                LinearLayout linearLayout = new LinearLayout(mContext);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.black));
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));

                RelativeLayout layout = new RelativeLayout(mContext);
//                layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue));

                RelativeLayout.LayoutParams layoutLayoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

                layoutLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                layout.setLayoutParams(layoutLayoutParams);

                Button closeButton = getCloseButtonStyle();

                RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                        (int) (20 * mContext.getResources().getDisplayMetrics().density),
                        (int) (20 * mContext.getResources().getDisplayMetrics().density));
                buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                // 設置偏移量，將按鈕向左偏移
                buttonParams.setMargins(
                        0,
                        (int) (5 * mContext.getResources().getDisplayMetrics().density),
                        (int) (5 * mContext.getResources().getDisplayMetrics().density),
                        (int) (5 * mContext.getResources().getDisplayMetrics().density));

                layout.addView(closeButton, buttonParams);

                linearLayout.addView(layout);


                final WebView webView = new WebView(mContext);

                webView.setBackgroundColor(Color.WHITE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                webView.getSettings().setDomStorageEnabled(true);

                webView.setFocusable(true);
                webView.setFocusableInTouchMode(true);
                webView.requestFocus(View.FOCUS_DOWN);

                WebView.setWebContentsDebuggingEnabled(true);

                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 使用緩存的方式

                webView.setWebViewClient(new WebViewClient());


                webView.loadUrl(url);

                linearLayout.addView(webView, new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                ));

                closeButton.setOnClickListener(v -> {
                    webView.destroy();
                    floatingWindow.destroy();
                });

                cardView.addView(linearLayout);

                viewGroup.addView(cardView);
            }

            @Override
            public void onClose() {

            }

            @Override
            public void willOpen(View view) {

            }

            @Override
            public void didOpen(View view) {

            }

            @Override
            public void willClose(View view) {

            }

            @Override
            public void didClose(View view) {

            }

        };

        floatingWindow.setFloatingListener(floatingListener);
        floatingWindow.create();

    }

    private Button getCloseButtonStyle(){

        int sp30 = (int) (30 * mContext.getResources().getDisplayMetrics().density);

        Button closeButton = new Button(mContext);

        // 取得svg
        Drawable vectorDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_x);

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(Color.parseColor("#F44336")); // 背景顏色
        backgroundDrawable.setCornerRadius(sp30); // 設定圓角半徑

        // 生成LayerDrawable並設定圖形、背景顏色
        Drawable[] layers = {backgroundDrawable, vectorDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(layers);

        // 設定圖形顯示的位置
        int inset = 0;
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);

        // 把LayerDrawable設定為按鈕背景
        closeButton.setBackground(layerDrawable);

        closeButton.setWidth(sp30);
        closeButton.setHeight(sp30);
        closeButton.setGravity(Gravity.CENTER);

        return closeButton;

    }


    private void CreateWidget(){

//         浮動視窗class example
//        FloatingLayoutConfig config =
//                new FloatingLayoutConfig.Builder(mContext)
//                        .setLayoutRes(R.layout.info_layout)
//                        .setMovementModule(-1)
//                        .setGravity(Gravity.END | Gravity.TOP)
//                        .setX(0)
//                        .setY(0)
//                        .setWidth(WindowManager.LayoutParams.WRAP_CONTENT)
//                        .setHeight(WindowManager.LayoutParams.MATCH_PARENT)
//                        .setShow(false)
//                        .setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
//                        .build();
//
//        FloatingLayout floatingWindow = new FloatingLayout(config);
//
//        FloatingLayoutConfig config2 =
//                new FloatingLayoutConfig.Builder(mContext)
//                        .setLayoutRes(-1)
//                        .setMovementModule(MOVE_AXIS_Y)
//                        .setGravity(Gravity.END | Gravity.TOP)
////                        .setX(16)
//                        .setY(200)
//                        .setWidth(WindowManager.LayoutParams.WRAP_CONTENT)
//                        .setHeight(WindowManager.LayoutParams.WRAP_CONTENT)
//                        .setShow(true)
//                        .build();
//
//        FloatingLayout floatingWindow2 = new FloatingLayout(config2);
//
//
//        FloatingListener floatingListener = new FloatingListener() {
//
//            @Override
//            public void onCreate(View view) {
//
//                setClock(view);
//                setCalenderView(view);
//                setAQI(view);
//                setLoopPhoto(view);
//                setUrlList(view);
////                setCountDown(view);
//
//                view.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        switch (event.getAction()) {
//
//                            case MotionEvent.ACTION_DOWN:
//
//                                return true;
//
//                            case MotionEvent.ACTION_OUTSIDE:
//
//                                floatingWindow.hide();
//                                floatingWindow2.show();
//
//                                return false;
//                            default:
//
//                                return false;
//                        }
//                    }
//                });
//
//            }
//
//            @Override
//            public void onClose() {
//
//            }
//
//            @Override
//            public void willOpen(View view) {
//                view.setAlpha(0.0f);
//            }
//
//            @Override
//            public void didOpen(View view) {
//
//                ViewGroup layout = view.findViewById(R.id.layout);
//
//                if(layout != null){
//
//                    // 建立TranslateAnimation對象
//                    TranslateAnimation translateAnimation = new TranslateAnimation(
//                            Animation.RELATIVE_TO_PARENT, 1.0f,  // 從右邊100%
//                            Animation.RELATIVE_TO_PARENT, 0.0f,  // 到0%
//                            Animation.RELATIVE_TO_PARENT, 0.0f,  // 從Y軸開始位置
//                            Animation.RELATIVE_TO_PARENT, 0.0f   // 到Y軸結束位置
//                    );
//
//                    // 設定動畫持續時間
//                    translateAnimation.setDuration(300);
//
//                    view.setAlpha(1.0f);
//                    layout.startAnimation(translateAnimation);
//
//
//                }else{
//
//                    view.setAlpha(1.0f);
//
//                }
//
//            }
//
//            @Override
//            public void willClose(View view) {
//
////                ViewGroup layout = view.findViewById(R.id.layout);
////
////                if(layout != null){
////
////                    CompletableFuture<Boolean> future = new CompletableFuture<>();
////
////                    // 建立TranslateAnimation對象
////                    TranslateAnimation translateAnimation = new TranslateAnimation(
////                            Animation.RELATIVE_TO_PARENT, 0.0f,  // 從右邊100%
////                            Animation.RELATIVE_TO_PARENT, 1.0f,  // 到0%
////                            Animation.RELATIVE_TO_PARENT, 0.0f,  // 從Y軸開始位置
////                            Animation.RELATIVE_TO_PARENT, 0.0f   // 到Y軸結束位置
////                    );
////
////                    // 設定動畫持續時間
////                    translateAnimation.setDuration(300);
////
////                    layout.startAnimation(translateAnimation);
////
////                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
////                        @Override
////                        public void onAnimationStart(Animation animation) {
////                            // 動畫開始時執行的操作
////                        }
////
////                        @Override
////                        public void onAnimationEnd(Animation animation) {
////                            // 動畫結束時執行的操作
////                            view.setAlpha(0.0f);
////                            future.complete(true);
////
////                        }
////
////                        @Override
////                        public void onAnimationRepeat(Animation animation) {
////                            // 動畫重複時執行的操作
////                        }
////                    });
////
////                    return future.join();
////
////                }
////
////                return true;
//
//            }
//
//            @Override
//            public void didClose(View view) {
//
//
//            }
//
//        };
//
//        floatingWindow.setFloatingListener(floatingListener);
//        floatingWindow.create();
//
//
//        FloatingListener floatingListener2 = new FloatingListener() {
//
//            @Override
//            public void onCreate(View view) {
//
//                LinearLayout getView = (LinearLayout) view;
//
//                // 创建RelativeLayout
//                RelativeLayout layout = new RelativeLayout(mContext);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                        10,  // 设置宽度
//                        96   // 设置高度
//                );
//                layout.setLayoutParams(params);
//
//                getView.setPadding(8,0,16,0);
//
//                // 设置背景颜色
//                int alpha = 255; // 半透明
//                int red = 78;
//                int green = 131;
//                int blue = 151;
//                int color = Color.argb(alpha, red, green, blue);
//
//                //創建帶有圓角和邊框的GradientDrawable
//                GradientDrawable drawable = new GradientDrawable();
//                drawable.setColor(color); // 背景颜色
//                float cornerRadius = 16 * mContext.getResources().getDisplayMetrics().density; // 圆角半径
//                drawable.setCornerRadius(cornerRadius);
//                drawable.setStroke(1, Color.parseColor("#afafafb3"));
//
//                // 设置drawable为layout的背景
//                layout.setBackground(drawable);
//
//                // 设置clipToOutline和outlineProvider
//                layout.setClipToOutline(true);
//                layout.setOutlineProvider(new ViewOutlineProvider() {
//                    @Override
//                    public void getOutline(View view, Outline outline) {
//                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
//                    }
//                });
//
//                getView.addView(layout);
//
//                view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        floatingWindow.show();
//                        floatingWindow2.hide();
//                    }
//                });
//
//            }
//
//            @Override
//            public void onClose() {
//
//            }
//
//            @Override
//            public void willOpen(View view) {
//
//            }
//
//            @Override
//            public void didOpen(View view) {
//
//            }
//
//            @Override
//            public void willClose(View view) {
//
//            }
//
//            @Override
//            public void didClose(View view) {
//
//            }
//
//        };
//
//        floatingWindow2.setFloatingListener(floatingListener2);
//        floatingWindow2.create();

        ///////////////////////////////////////////////////////////////////////////////////////

        FloatingLayoutConfig config3 =
                new FloatingLayoutConfig.Builder(mContext)
                        .setLayoutRes(-1)
                        .setMovementModule(FloatingViewMovementModule.MOVE_AXIS_XY)
                        .setGravity(Gravity.START | Gravity.TOP)
//                        .setX(16)
                        .setY(200)
                        .setWidth(WindowManager.LayoutParams.WRAP_CONTENT)
                        .setHeight(WindowManager.LayoutParams.WRAP_CONTENT)
                        .setShow(true)
                        .build();

        FloatingLayout floatingWindow3 = new FloatingLayout(config3);
        FloatingListener floatingListener3 = new FloatingListener() {

            @Override
            public void onCreate(View view) {

                ViewGroup viewGroup = (ViewGroup) view;

                RelativeLayout layout = new RelativeLayout(mContext);
                layout.setBackgroundColor(Color.parseColor("#02bb02"));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        96,  // 设置宽度
                        96   // 设置高度
                );
                layout.setLayoutParams(params);

                viewGroup.addView(layout);

                viewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

//                        String text = "123";
                        String text = "" +
                                "<div style='display:flex; flex-direction: column; align-items: center; justify-content: center; padding:.5rem;'>" +
                                    "<p style='font-size:3.5rem; margin:0;'>教務處廣播<></p>" +
                                    "<p style='font-size:3.5rem; margin:0;'> </p>" +
                                    "<p style='font-size:3.5rem; margin:0;'>請以下班級班長下課至教務處報到</p>" +
                                    "<p style='font-size:3.5rem; margin:0;'> </p>" +
                                    "<p style='font-size:3.5rem; margin:0;'>101 , 102 , 103 , 104 , 105 , 106 , 107 , " +
                                    "201 , 202 , 203 , 204 , 205 , 206 , 207 , " +
                                    "301 , 302 , 303 , 304 , 305 , 306 , 307</p>" +
                                "</div>";
//                        String text= "test123\n test123test123testtest123test123test123test123123test123\n test123\n test123test123test123\n test123test123test123\ntest123test123test123\n test123\n testtest123test123123\n testest123t123\n test123";
//                        String text= "13414132413413412341324134141132424342342342343141432412342134";

                        String value = "{\"time\":\"300\",\"timeType\":\"durationTime\",\"textSize\":\"50\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"mediaId\":1260684987,\"text\":\"" +text+ "\"}";

//                        String value = "{\"time\":\"300\",\"timeType\":\"durationTime\",\"width\":\"1300\",\"height\":\"200\",\"x\":\"50\",\"y\":\"100\",\"mediaId\":2110516110,\"url\":\"https://imds.tw/mdm/tool_web/audioPlayer.php?type=icrt&value=ICRTNK\",\"originalUrl\":\"ICRTNK\"}";
                        new Function_set_cmd().setTextBroadcast(value, mContext, "2024-06-07 09:52:55");

                    }
                });

            }

            @Override
            public void onClose() {

            }

            @Override
            public void willOpen(View view) {

            }

            @Override
            public void didOpen(View view) {

            }

            @Override
            public void willClose(View view) {

            }

            @Override
            public void didClose(View view) {

            }

        };

        floatingWindow3.setFloatingListener(floatingListener3);
        floatingWindow3.create();



        String text =

                "<html>" +
                    "<head>" +
                        "<meta charset='utf-8'>" +
                    "</head>" +
                    "<body style='margin: 0;'>"+
                        "<div style=''>" +
                            "<div style=''>" +
                                "<div style='font-size:3rem; max-height: 100%; text-align: center; color:rgb(255, 255, 255);'>" +
                                    "教務處廣播</br></br>" +
                                    "請以下班級班長下課至教務處報到</br></br>" +
                                    "101 , 102 , 103 , 104 , 105 , 106 , 107 ,</br>" +
                                    "201 , 202 , 203 , 204 , 205 , 206 , 207 ,</br>" +
                                    "301 , 302 , 303 , 304 , 305 , 306 , 307,</br>" +
                                    "401 , 402 , 403 , 404 , 405 , 406 , 407,</br>" +
                                    "501 , 502 , 503 , 504 , 505 , 506 , 507,</br>" +
                                    "601 , 602 , 603 , 604 , 605 , 606 , 607,</br>" +
                                    "101 , 102 , 103 , 104 , 105 , 106 , 107 ,</br>" +
                                    "201 , 202 , 203 , 204 , 205 , 206 , 207 ,</br>" +
                                    "301 , 302 , 303 , 304 , 305 , 306 , 307,</br>" +
                                    "401 , 402 , 403 , 404 , 405 , 406 , 407,</br>" +
                                    "501 , 502 , 503 , 504 , 505 , 506 , 507,</br>" +
                                    "601 , 602 , 603 , 604 , 605 , 606 , 607" +
                                "</div>" +
                            "</div>" +
                        "</div>" +
                    "</body>" +
                "</html>";



        String value = "{\"time\":\"3000\",\"timeType\":\"durationTime\",\"textSize\":\"50\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"mediaId\":1260684987,\"text\":\"" +text+ "\"}";

//                        String value = "{\"time\":\"300\",\"timeType\":\"durationTime\",\"width\":\"1300\",\"height\":\"200\",\"x\":\"50\",\"y\":\"100\",\"mediaId\":2110516110,\"url\":\"https://imds.tw/mdm/tool_web/audioPlayer.php?type=icrt&value=ICRTNK\",\"originalUrl\":\"ICRTNK\"}";
        new Function_set_cmd().setTextBroadcast(value, mContext, "2024-06-07 09:52:55");

//        String text= "test123";
//                        String text= "13414132413413412341324134141132424342342342343141432412342134";
//
//        String value = "{\"time\":\"10\",\"timeType\":\"durationTime\",\"textSize\":\"50\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"setPosition\":\"top\",\"mediaId\":1260684987,\"text\":\"" +text+ "\"}";
//
//        String value = "{\"time\":\"300\",\"timeType\":\"durationTime\",\"width\":\"300\",\"height\":\"200\",\"x\":\"50\",\"y\":\"100\",\"mediaId\":2110516110,\"url\":\"https://imds.tw/mdm/tool_web/audioPlayer.php?type=icrt&value=ICRTNK\",\"originalUrl\":\"ICRTNK\"}";
//        new Function_set_cmd().F_set_marquee(value, mContext, "2024-06-07 09:52:55");
//
//
//        String value = "{\"x\":\"0\",\"y\":\"0\",\"url\":\"https://imds.tw/mdm/php/mdm_redirect.php?data=YUhSMGNITTZMeTlwYldSekxuUjNMMjFrYlM5amJHOTFaRjl6Y0dGalpTOXRaRzFmZEdWemRETXZabWxzWlY4eE56RTNOalUwTmpNMExtcHdadz09\",\"time\":\"300\",\"width\":\"-1\",\"height\":\"-1\",\"mediaId\":661732000,\"timeType\":\"durationTime\",\"originalUrl\":\"https://imds.tw/mdm/php/mdm_redirect.php?data=YUhSMGNITTZMeTlwYldSekxuUjNMMjFrYlM5amJHOTFaRjl6Y0dGalpTOXRaRzFmZEdWemRETXZabWxzWlY4eE56RTNOalUwTmpNMExtcHdadz09\"}";
//        new Function_set_cmd().F_set_image(value, mContext, "2024-06-07 09:52:55");
//
//        String a = "{\"mediaId\":1131476566}";
//        new Function_set_cmd().F_stop_floating_window(a, mContext);

    }
}

// 整理行事曆清單
class Event {

    public String title;
    public String content;
    public LocalDateTime startDateTime;
    public LocalDateTime endDateTime;
    public String location;

    public Event(String title, String content, String startDate, String startTime, String endDate, String endTime, String location) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.title = title;
        this.content = content;
        this.startDateTime = LocalDateTime.of(LocalDate.parse(startDate, dateFormatter), LocalTime.parse(startTime, timeFormatter));
        this.endDateTime = LocalDateTime.of(LocalDate.parse(endDate, dateFormatter), LocalTime.parse(endTime, timeFormatter));
        this.location = location;

    }

}

class DailyEvent {
    Event event;
    String startTime;
    String endTime;

    public DailyEvent(Event event, String startTime, String endTime) {
        this.event = event;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

class AutoScroll {

    final ViewPager2 viewPager;
    final ImageSliderAdapter adapter;
    final int intervalTime;
    final int itemCount;

    int nowPageNum = 0;
    boolean isStartTimer = false;

    private Runnable autoScrollRunnable;


    public AutoScroll(ViewPager2 viewPager, ImageSliderAdapter adapter, int intervalTime){

        this.viewPager = viewPager;
        this.adapter = adapter;
        this.intervalTime = intervalTime;
        this.itemCount = adapter.getItemCount();

        startLoop();
    }

    private void startLoop(){

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {

                int nextItem = (nowPageNum + 1) % itemCount;

                isStartTimer = false;
                viewPager.setCurrentItem(nextItem, true);

            }
        };

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                nowPageNum = position;

                if(!isStartTimer){

                    isStartTimer = true;

                    viewPager.postDelayed(autoScrollRunnable, intervalTime);

                }else {

                    viewPager.removeCallbacks(autoScrollRunnable);
                    viewPager.postDelayed(autoScrollRunnable, intervalTime);
                }

            }
        });

    }

}