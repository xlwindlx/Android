package com.example.renya.myapplication_ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Renya on 2016-11-01.
 */
public class StartActivity extends AppCompatActivity {

    private final String[] navItems = {"이미지 보기", "동영상 보기"};
    private ListView lvNavList;
    private DrawerLayout dlDrawer;


    @Override // 뒤로가기 버튼 누를시 메뉴창 닫힘 기능 구현
    public void onBackPressed() {
        if (dlDrawer.isDrawerOpen(lvNavList)) {
            dlDrawer.closeDrawer(lvNavList);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        lvNavList = (ListView)findViewById(R.id.lv_activity_main_nav_list);
        dlDrawer = (DrawerLayout)findViewById(R.id.relativeLO);
        lvNavList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navItems));
        lvNavList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        // 리스트 뷰의 아이템을 골랐을때 수행하는 작업들
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            switch (position) {
                case 0:
                    //액티비티 전환 - 이미지 리스트
                    Toast.makeText(getApplicationContext(),"Image List", Toast.LENGTH_SHORT).show();
                    Intent intentImg = new Intent(getApplicationContext(),ImageListActivity.class);
                    startActivity(intentImg);
                    break;
                case 1:
                    //액티비티 전환 - 동영상 리스트
                    Toast.makeText(getApplicationContext(),"Video List", Toast.LENGTH_SHORT).show();
                    Intent intentVid = new Intent(getApplicationContext(),VideoListActivity.class);
                    startActivity(intentVid);
                    break;
            }
            dlDrawer.closeDrawer(lvNavList);
        }
    }
}