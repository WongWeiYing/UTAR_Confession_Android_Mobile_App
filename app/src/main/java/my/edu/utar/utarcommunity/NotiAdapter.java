package my.edu.utar.utarcommunity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.UUID;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.ViewHolder> {
    private final List<NotiItem> notiItem;
    private UUID user_ID;
    public NotiAdapter(List<NotiItem> notiItem, UUID user_ID) {
        this.notiItem = notiItem;
        this.user_ID = user_ID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_noti, parent, false);
        return new NotiAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NotiItem notiitem = notiItem.get(position);
        holder.userimage.setImageResource(notiitem.getUserimage());
        holder.username.setText(notiitem.getUsername());
        holder.time.setText(notiitem.getTime());
        holder.post.setText(notiitem.getPost());
        holder.action.setText(notiitem.getNoti_Action());

        holder.itemView.setOnClickListener(e -> {

            Intent intent = new Intent(holder.itemView.getContext(), CommentArea.class);
            intent.putExtra("user_ID", user_ID.toString());
            intent.putExtra("post_ID", notiitem.getID());
            intent.putExtra("user_DisplayName", notiitem.getUsername());
            holder.itemView.getContext().startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return notiItem.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userimage;
        TextView username;
        TextView time;
        TextView post;
        TextView action;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userimage = itemView.findViewById(R.id.noti_userimage);
            username = itemView.findViewById(R.id.noti_username);
            time = itemView.findViewById(R.id.noti_time);
            post = itemView.findViewById(R.id.noti_post);
            action = itemView.findViewById(R.id.noti_action);

        }

    }

}
