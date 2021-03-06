package support.im.mobilecontact;

import java.util.Collections;
import java.util.List;
import support.im.data.MobileContact;
import support.im.data.source.MobileContactsDataSource;
import support.im.data.source.MobileContactsRepository;
import support.im.mobilecontact.pinyin.CharacterParser;
import support.im.mobilecontact.pinyin.PinyinComparator;

import static com.google.common.base.Preconditions.checkNotNull;

public class MobileContactsPresenter implements MobileContactsContract.Presenter {

  private final MobileContactsRepository mMobileContactsRepository;
  private final MobileContactsContract.View mMobileContactsView;

  private CharacterParser characterParser;
  private PinyinComparator pinyinComparator;

  private boolean mFirstLoad = true;

  public MobileContactsPresenter(MobileContactsRepository mobileContactsRepository,
      MobileContactsContract.View mobileContactsView) {
    mMobileContactsRepository = checkNotNull(mobileContactsRepository);
    mMobileContactsView = checkNotNull(mobileContactsView);

    characterParser = CharacterParser.getInstance();
    pinyinComparator = new PinyinComparator();

    mobileContactsView.setPresenter(this);
  }

  @Override public void start() {
    loadMobileContacts(false);
  }

  @Override public void loadMobileContacts(boolean forceUpdate) {
    // Simplification for sample: a network reload will be forced on first load.
    loadMobileContacts(forceUpdate || mFirstLoad, true);
    mFirstLoad = false;
  }

  /**
   * @param forceUpdate   Pass in true to refresh the data in the {@link MobileContactsDataSource}
   * @param showLoadingUI Pass in true to display a loading icon in the UI
   */
  private void loadMobileContacts(boolean forceUpdate, final boolean showLoadingUI) {
    if (showLoadingUI) {
      mMobileContactsView.setLoadingIndicator(true);
    }
    if (forceUpdate) {
      mMobileContactsRepository.refreshMobileContacts();
    }

    mMobileContactsRepository.getMobileContacts(new MobileContactsDataSource.LoadMobileContactsCallback() {
      @Override public void onMobileContactsLoaded(List<MobileContact> mobileContacts) {
        for (MobileContact mobileContact : mobileContacts) {
          String pinyin = characterParser.getSelling(mobileContact.getName());
          String sortString = pinyin.substring(0, 1).toUpperCase();
          if (sortString.matches("[A-Z]")) {
            mobileContact.setSortLetters(sortString.toUpperCase());
          } else {
            mobileContact.setSortLetters("#");
          }
        }
        Collections.sort(mobileContacts, pinyinComparator);

        // The view may not be able to handle UI updates anymore
        if (!mMobileContactsView.isActive()) {
          return;
        }
        if (showLoadingUI) {
          mMobileContactsView.setLoadingIndicator(false);
        }
        processMobileContacts(mobileContacts);
      }
    });
  }

  private void processMobileContacts(List<MobileContact> mobileContacts) {
    if (mobileContacts.isEmpty()) {
      // Show a message indicating there are no tasks for that filter type.
      processEmptyTasks();
    } else {
      // Show the list of tasks
      mMobileContactsView.showMobileContacts(mobileContacts);
    }
  }

  private void processEmptyTasks() {
    mMobileContactsView.showNoMobileContacts();
  }
}
