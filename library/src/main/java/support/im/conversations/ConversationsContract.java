package support.im.conversations;

import com.avos.avoscloud.AVException;
import java.util.List;
import support.im.BasePresenter;
import support.im.BaseView;
import support.im.data.ConversationModel;

public interface ConversationsContract {

  interface View extends BaseView<Presenter> {
    void setLoadingIndicator(boolean active);

    void notifyDataSetChanged(List<ConversationModel> conversations);

    void notifyItemChanged(ConversationModel conversation);

    void showNoConversations();

    void showError(String error, AVException e);

    boolean isActive();
  }

  interface Presenter extends BasePresenter {
    void loadConversations(boolean forceUpdate);
  }
}
