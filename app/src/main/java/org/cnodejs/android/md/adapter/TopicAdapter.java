package org.cnodejs.android.md.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.cnodejs.android.md.R;
import org.cnodejs.android.md.activity.UserDetailActivity;
import org.cnodejs.android.md.listener.WebViewContentClient;
import org.cnodejs.android.md.model.api.ApiClient;
import org.cnodejs.android.md.model.entity.Reply;
import org.cnodejs.android.md.model.entity.TopicUpInfo;
import org.cnodejs.android.md.model.entity.TopicWithReply;
import org.cnodejs.android.md.storage.LoginShared;
import org.cnodejs.android.md.util.FormatUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import us.feras.mdv.MarkdownView;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_REPLY = 1;

    private Context context;
    private LayoutInflater inflater;
    private TopicWithReply topic;

    private boolean isHeaderShow = false; // TODO 当false时，渲染header，其他时间不渲染

    private WebViewClient webViewClient;

    public TopicAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        this.webViewClient = new WebViewContentClient(context);
    }

    public void setTopic(TopicWithReply topic) {
        this.topic = topic;
        isHeaderShow = false;
    }

    @Override
    public int getItemCount() {
        return topic == null ? 0 : topic.getReplies().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (topic != null && position != 0) {
            return TYPE_REPLY;
        } else {
            return TYPE_HEADER;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.activity_topic_item_header, parent, false));
            default: // TYPE_REPLY
                return new ReplyViewHolder(inflater.inflate(R.layout.activity_topic_item_reply, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                holder.update(position);
                break;
            default: // TYPE_REPLY
                holder.update(position - 1);
                break;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        protected ViewHolder(View itemView) {
            super(itemView);
        }

        protected void update(int position) {}

    }

    public class HeaderViewHolder extends ViewHolder {

        @Bind(R.id.topic_item_header_tv_title)
        protected TextView tvTitle;

        @Bind(R.id.topic_item_header_tv_tab)
        protected TextView tvTab;

        @Bind(R.id.topic_item_header_tv_visit_count)
        protected TextView tvVisitCount;

        @Bind(R.id.topic_item_header_img_avatar)
        protected ImageView imgAvatar;

        @Bind(R.id.topic_item_header_tv_login_name)
        protected TextView tvLoginName;

        @Bind(R.id.topic_item_header_tv_create_time)
        protected TextView tvCreateTime;

        @Bind(R.id.topic_item_header_btn_collect)
        protected ImageView btnCollect;

        @Bind(R.id.topic_item_header_web_content)
        protected MarkdownView webReplyContent;

        @Bind(R.id.topic_item_header_icon_good)
        protected View iconGood;

        @Bind(R.id.topic_item_header_layout_no_reply)
        protected ViewGroup layoutNoReply;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            webReplyContent.setWebViewClient(webViewClient); // TODO 对内连接做分发
        }

        public void update(int position) {
            if (!isHeaderShow) {
                tvTitle.setText(topic.getTitle());
                tvTab.setText(topic.isTop() ? R.string.tab_top : topic.getTab().getNameId());
                tvTab.setBackgroundResource(topic.isTop() ? R.drawable.topic_tab_top_background : R.drawable.topic_tab_normal_background);
                tvTab.setTextColor(context.getResources().getColor(topic.isTop() ? android.R.color.white : R.color.text_color_secondary));
                tvVisitCount.setText(topic.getVisitCount() + "次浏览");
                Picasso.with(context).load(ApiClient.ROOT_HOST + topic.getAuthor().getAvatarUrl()).error(R.drawable.image_default).into(imgAvatar);
                tvLoginName.setText(topic.getAuthor().getLoginName());
                tvCreateTime.setText(context.getString(R.string.post_at_$) + FormatUtils.getRecentlyTimeFormatText(topic.getCreateAt()));
                iconGood.setVisibility(topic.isGood() ? View.VISIBLE : View.GONE);
                layoutNoReply.setVisibility(topic.getReplies().size() > 0 ? View.GONE : View.VISIBLE);

                // TODO 是否收藏标记


                // TODO 这里直接使用WebView，有性能问题
                webReplyContent.loadMarkdown(topic.makeSureAndGetFilterContent());

                isHeaderShow = true;
            }
        }

        @OnClick(R.id.topic_item_header_img_avatar)
        protected void onBtnAvatarClick() {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("loginName", topic.getAuthor().getLoginName());
            context.startActivity(intent);
        }

        @OnClick(R.id.topic_item_header_btn_collect)
        protected void onBtnCollectClick() {

            // TODO 收藏按钮

        }

    }

    public class ReplyViewHolder extends ViewHolder {

        @Bind(R.id.topic_item_reply_img_avatar)
        protected ImageView imgAvatar;

        @Bind(R.id.topic_item_reply_tv_login_name)
        protected TextView tvLoginName;

        @Bind(R.id.topic_item_reply_tv_index)
        protected TextView tvIndex;

        @Bind(R.id.topic_item_reply_tv_create_time)
        protected TextView tvCreateTime;

        @Bind(R.id.topic_item_reply_btn_ups)
        protected TextView btnUps;

        @Bind(R.id.topic_item_reply_web_content)
        protected MarkdownView webReplyContent;

        @Bind(R.id.topic_item_reply_icon_deep_line)
        protected View iconDeepLine;

        @Bind(R.id.topic_item_reply_icon_shadow_gap)
        protected View iconShadowGap;

        private Reply reply;
        private int position = -1;

        public ReplyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            webReplyContent.setWebViewClient(webViewClient); // TODO 对内连接做分发
        }

        public void update(int position) {
            this.position = position;
            reply = topic.getReplies().get(position);

            Picasso.with(context).load(ApiClient.ROOT_HOST + reply.getAuthor().getAvatarUrl()).error(R.drawable.image_default).into(imgAvatar);
            tvLoginName.setText(reply.getAuthor().getLoginName());
            tvIndex.setText(position + 1 + "楼");
            tvCreateTime.setText(FormatUtils.getRecentlyTimeFormatText(reply.getCreateAt()));
            btnUps.setText(String.valueOf(reply.getUps().size()));
            btnUps.setCompoundDrawablesWithIntrinsicBounds(reply.getUps().contains(LoginShared.getId(context)) ? R.drawable.main_nav_ic_good_theme_24dp : R.drawable.main_nav_ic_good_grey_24dp, 0, 0, 0);
            iconDeepLine.setVisibility(position == topic.getReplies().size() - 1 ? View.GONE : View.VISIBLE);
            iconShadowGap.setVisibility(position == topic.getReplies().size() - 1 ? View.VISIBLE : View.GONE);

            // TODO 这里直接使用WebView，有性能问题
            webReplyContent.loadMarkdown(reply.makeSureAndGetFilterContent());
        }

        @OnClick(R.id.topic_item_reply_img_avatar)
        protected void onBtnAvatarClick() {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("loginName", reply.getAuthor().getLoginName());
            context.startActivity(intent);
        }

        @OnClick(R.id.topic_item_reply_btn_ups)
        protected void onBtnUpsClick() {
            upTopicAsyncTask(this);
        }

        @OnClick(R.id.topic_item_reply_btn_at)
        protected void onBtnAtClick() {
            // TODO
        }

    }

    private void upTopicAsyncTask(final ReplyViewHolder holder) {
        final int position = holder.position; // 标记当时的位置信息
        ApiClient.service.upTopic(LoginShared.getAccessToken(context), holder.reply.getId(), new Callback<TopicUpInfo>() {

            @Override
            public void success(TopicUpInfo info, Response response) {
                if (position == holder.position) { // 位置没有变
                    if (info.getAction() == TopicUpInfo.Action.up) {
                        holder.reply.getUps().add(LoginShared.getId(context));
                    } else if (info.getAction() == TopicUpInfo.Action.down) {
                        holder.reply.getUps().remove(LoginShared.getId(context));
                    }
                    holder.btnUps.setText(String.valueOf(holder.reply.getUps().size()));
                    holder.btnUps.setCompoundDrawablesWithIntrinsicBounds(holder.reply.getUps().contains(LoginShared.getId(context)) ? R.drawable.main_nav_ic_good_theme_24dp : R.drawable.main_nav_ic_good_grey_24dp, 0, 0, 0);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, "网络访问失败，请重试", Toast.LENGTH_SHORT).show();
            }

        });
    }

}
