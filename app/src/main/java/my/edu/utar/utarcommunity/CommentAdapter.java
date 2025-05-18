package my.edu.utar.utarcommunity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<CommentLayout> commentLayout;
    private final Context context;

    public CommentAdapter(Context context, List<CommentLayout> commentLayout) {
        this.context = context;
        this.commentLayout = commentLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_comment, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CommentLayout commentItem = commentLayout.get(position);
        holder.userimage.setImageResource(commentItem.getUserimage());
        holder.username.setText(commentItem.getUsername());
        holder.time.setText(commentItem.getTime());
        holder.comments.setText(commentItem.getComments());
    }

    @Override
    public int getItemCount() {
        return commentLayout.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userimage;
        TextView username;
        TextView time;
        TextView comments;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userimage = itemView.findViewById(R.id.comment_image);
            username = itemView.findViewById(R.id.comment_username);
            time = itemView.findViewById(R.id.comment_time);
            comments = itemView.findViewById(R.id.comment);
        }
    }
}
