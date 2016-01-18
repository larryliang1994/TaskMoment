package com.jiubai.taskmoment.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.common.UtilBox;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
public class CheckPictureActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.vp_checkPicture)
    ViewPager vp;

    @Bind(R.id.iBtn_tool)
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

        UtilBox.setStatusBarTint(this, R.color.checkPicture);

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
        // net则不需要toolbar
        if ("local".equals(fromWhere)) {
            tv_title.setText(R.string.checkPicture);
            iBtn_delete.setVisibility(View.VISIBLE);
            iBtn_delete.setImageResource(R.drawable.delete);
        } else if ("net".equals(fromWhere)) {
            toolBar.setVisibility(View.GONE);
        }

        vp.setOffscreenPageLimit(1);
        vp.setAdapter(new SamplePagerAdapter());
        vp.setCurrentItem(index, true);
    }

    @Override
    @OnClick({R.id.iBtn_tool, R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_tool:
                final MaterialDialog dialog = new MaterialDialog(this);
                dialog.setTitle("提示")
                        .setMessage("要删除这张照片吗?")
                        .setCanceledOnTouchOutside(true)
                        .setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                hasChange = true;

                                int currentItem = vp.getCurrentItem();

                                pictureList.remove(currentItem);

                                if (pictureList.size() != 0) {
                                    vp.setAdapter(new SamplePagerAdapter());
                                    vp.setCurrentItem(currentItem - 1);
                                } else {
                                    if (hasChange) {
                                        Intent intent = new Intent();
                                        intent.putExtra("pictureList", pictureList);
                                        setResult(RESULT_OK, intent);
                                    } else {
                                        setResult(RESULT_CANCELED);
                                    }

                                    CheckPictureActivity.this.finish();
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
                if (hasChange) {
                    Intent intent = new Intent();
                    intent.putExtra("pictureList", pictureList);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }

                finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.zoom_out_quick);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (hasChange) {
                Intent intent = new Intent();
                intent.putExtra("pictureList", pictureList);
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }

            finish();
            overridePendingTransition(R.anim.scale_stay, R.anim.zoom_out_quick);

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
            final RelativeLayout layout = new RelativeLayout(CheckPictureActivity.this);

            final RelativeLayout.LayoutParams photoRlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            photoRlp.addRule(RelativeLayout.CENTER_IN_PARENT);

            final ProgressBar progressBar = new ProgressBar(CheckPictureActivity.this);
            final PhotoView photoView = new PhotoView(container.getContext());

            if (!pictureList.get(position).contains("http")) {
                progressBar.setVisibility(View.GONE);
                String imgUrl = ImageDownloader.Scheme.FILE.wrap(pictureList.get(position));
                ImageLoader.getInstance().displayImage(imgUrl, photoView);
            } else {
                ImageLoader.getInstance().displayImage(
                        pictureList.get(position)+ "?t=" + Config.TIME,
                        photoView,
                        new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String s, View view) {
                            }

                            @Override
                            public void onLoadingFailed(String s, View view, FailReason failReason) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(CheckPictureActivity.this,
                                        "图片加载出错", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingCancelled(String s, View view) {
                            }
                        });
            }

            // 单击退出
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    if (hasChange) {
                        Intent intent = new Intent();
                        intent.putExtra("pictureList", pictureList);
                        setResult(RESULT_OK, intent);
                    } else {
                        setResult(RESULT_CANCELED);
                    }

                    CheckPictureActivity.this.finish();
                    overridePendingTransition(R.anim.scale_stay, R.anim.zoom_out_quick);
                }
            });
            layout.addView(photoView, photoRlp);

            RelativeLayout.LayoutParams progressRlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            progressRlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(progressBar, progressRlp);

            container.addView(layout,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return layout;
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