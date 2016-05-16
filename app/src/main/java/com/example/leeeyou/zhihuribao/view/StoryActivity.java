package com.example.leeeyou.zhihuribao.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;

import com.example.leeeyou.zhihuribao.R;
import com.example.leeeyou.zhihuribao.data.model.RiBao;
import com.example.leeeyou.zhihuribao.data.model.Story2;
import com.example.leeeyou.zhihuribao.di.component.DaggerStoryComponent;
import com.example.leeeyou.zhihuribao.di.module.StoryModule;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class StoryActivity extends Base_Original_Activity {

    @BindView(R.id.lv_zhihuribao)
    ListView lv_zhihuribao;

    @Inject
    Observable<RiBao> storyObservable;

    UniversalAdapter<Story2> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(StoryActivity.this);
        setLeftTitleAndDoNotDisplayHomeAsUp("知乎日报");

        getStories();
    }

    @Override
    void setupActivityComponent() {
        DaggerStoryComponent
                .builder()
                .storyModule(new StoryModule())
                .build()
                .inject(this);
    }

    private void getStories() {
        storyObservable.subscribeOn(Schedulers.newThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<RiBao, Boolean>() {
                    @Override
                    public Boolean call(RiBao riBao) {
                        StringBuilder sb = new StringBuilder();
                        char[] chars = riBao.date.toCharArray();
                        for (int i = 0; i < chars.length; i++) {
                            if (i == 4 || i == 6) {
                                sb.append("-");
                            }
                            sb.append(chars[i]);
                        }

                        List<Story2> stories = riBao.stories;
                        for (Story2 story2 : stories) {
                            story2.date = sb.toString();
                        }

                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RiBao>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(RiBao ribao) {
                        setAdapter(ribao.stories);
                    }
                });
    }

    private void setAdapter(@NonNull List<Story2> stories) {
        if (mAdapter == null) {
            mAdapter = new UniversalAdapter<Story2>(StoryActivity.this, stories, R.layout.item_lv_story) {
                @Override
                public void convert(ViewHolder vh, Story2 story2, int position) {
                    vh.setText(R.id.tv_story_title, story2.title);
                    vh.setText(R.id.tv_story_time, story2.date);
                    vh.setImageByUrl(R.id.iv_story_image, story2.images.get(0));
                }
            };
            lv_zhihuribao.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @OnItemClick(R.id.lv_zhihuribao)
    public void onItemClick(int position) {
        Story2 story2 = (Story2) lv_zhihuribao.getItemAtPosition(position);
        startActivity(new Intent()
                .setClass(this, StoryDetailActivity.class)
                .putExtra("storyId", story2.id)
                .putExtra("storyTitle", story2.title));
    }

}