package com.jiubai.taskmoment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.UtilBox;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 查看图片
 */
public class Aty_CheckPicture extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.vp_checkPicture)
    ViewPager vp;

    @Bind(R.id.iBtn_delete)
    ImageButton iBtn_delete;

    @Bind(R.id.toolBar)
    LinearLayout toolBar;

    private int index = 0;
    private ArrayList<String> pictureList;
    private String fromWhere;
    private boolean hasChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_checkpicture);

        pictureList = getIntent().getStringArrayListExtra("pictureList");
        index = getIntent().getIntExtra("index", 0);
        fromWhere = getIntent().getStringExtra("fromWhere");

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // net则来自timeline，不需要toolbar
        if("local".equals(fromWhere)) {
            tv_title.setText(R.string.checkPicture);
            iBtn_delete.setVisibility(View.VISIBLE);
        } else if ("net".equals(fromWhere)){
            toolBar.setVisibility(View.GONE);
        }

        vp.setOffscreenPageLimit(1);
        vp.setAdapter(new SamplePagerAdapter());
        vp.setCurrentItem(index, true);
    }

    @Override
    @OnClick({R.id.iBtn_delete, R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_delete:
                final MaterialDialog dialog = new MaterialDialog(this);
                dialog.setTitle("提示")
                        .setMessage("要删除这张照片吗?")
                        .setCanceledOnTouchOutside(true)
                        .setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                hasChange = true;

                                pictureList.remove(vp.getCurrentItem());

                                if(pictureList.size() != 1) {
                                    vp.setAdapter(new SamplePagerAdapter());
                                } else {
                                    if(hasChange) {
                                        Intent intent = new Intent();
                                        intent.putExtra("pictureList", pictureList);
                                        setResult(RESULT_OK, intent);
                                    } else {
                                        setResult(RESULT_CANCELED);
                                    }

                                    Aty_CheckPicture.this.finish();
                                }
                            }
                        })
                        .setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;

            case R.id.iBtn_back:
                if(hasChange) {
                    Intent intent = new Intent();
                    intent.putExtra("pictureList", pictureList);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }

                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(hasChange) {
                Intent intent = new Intent();
                intent.putExtra("pictureList", pictureList);
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }

            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pictureList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            if ("local".equals(fromWhere)) { // 本地图片
                photoView.setImageBitmap(UtilBox.getLocalBitmap(pictureList.get(position),
                        UtilBox.getWidthPixels(Aty_CheckPicture.this),
                        UtilBox.getHeightPixels(Aty_CheckPicture.this)));
            } else if ("net".equals(fromWhere)) { // 网络图片
                ImageLoader.getInstance().displayImage(pictureList.get(position), photoView);
            }

            // 单击退出
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    if(hasChange) {
                        Intent intent = new Intent();
                        intent.putExtra("pictureList", pictureList);
                        setResult(RESULT_OK, intent);
                    } else {
                        setResult(RESULT_CANCELED);
                    }

                    Aty_CheckPicture.this.finish();
                }
            });

            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
