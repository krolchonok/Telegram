/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.ushastoe.fluffy.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.*;
import org.telegram.ui.*;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.ushastoe.fluffy.BulletinHelper;
import org.ushastoe.fluffy.activities.elements.headerSettingsCell;
import org.ushastoe.fluffy.fluffyConfig;
import org.ushastoe.fluffy.helpers.WhisperHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;
import static org.ushastoe.fluffy.fluffyConfig.writeCamera;

public class fluffySettingsActivity extends BaseFragment {

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager layoutManager;

    private headerSettingsCell headerSettingsCell;
    private int chatSettingsSectionRow;
    private int appearanceSettingsSectionRow;
    private int otherSettingsSectionRow;
    private int cameraSelectRow;
    private int localPremiumRow;
    private int zodiacShowRow;
    private int storiesShowRow;
    private int callShowRow;
    private int centerTitleRow;
    private int downloadSpeedBoostRow;
    private int selectTitleRow;

    private int voiceUseCloudflareRow;
    private int cfCredentialsRow;
    private int disableRoundRow;
    private int systemTypefaceRow;
    private int useSolarIconsRow;
    private int formatTimeWithSecondsRow;
    private int doubleTapInActionRow;
    private int doubleTapOutActionRow;
    private int moreInfoOnlineRow;
    private int unmuteVideoWithVolumeRow;
    private int saveEditRow;
    private int saveDelRow;

    private int aboutFluffyRow;
    private int rowCount;

    private boolean updateCameraSelect;


    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        DownloadController.getInstance(currentAccount).loadAutoDownloadConfig(true);
        updateRows(true);

        return true;
    }

    private void updateRows(boolean fullNotify) {
        rowCount = 0;
        chatSettingsSectionRow = rowCount++;
        voiceUseCloudflareRow = rowCount++;
        cfCredentialsRow = rowCount++;
        cameraSelectRow = rowCount++;
        zodiacShowRow = rowCount++;

        appearanceSettingsSectionRow = rowCount++;
        selectTitleRow = rowCount++;
        storiesShowRow = rowCount++;
        callShowRow = rowCount++;
        centerTitleRow = rowCount++;
        disableRoundRow = rowCount++;
        systemTypefaceRow = rowCount++;
        useSolarIconsRow = rowCount++;
        formatTimeWithSecondsRow = rowCount++;
        doubleTapInActionRow = rowCount++;
        doubleTapOutActionRow = rowCount++;
        moreInfoOnlineRow = rowCount++;
        unmuteVideoWithVolumeRow = rowCount++;
        saveDelRow = rowCount++;
        saveEditRow = rowCount++;

        otherSettingsSectionRow = rowCount++;
        localPremiumRow = rowCount++;
        downloadSpeedBoostRow = rowCount++;
        if (listAdapter != null && fullNotify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void rebind(int position) {
        if (listView == null || listAdapter == null) {
            return;
        }
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            RecyclerView.ViewHolder holder = listView.getChildViewHolder(child);
            if (holder != null && holder.getAdapterPosition() == position) {
                listAdapter.onBindViewHolder(holder, position);
                return;
            }
        }
    }

    private void rebindAll() {
        if (listView == null || listAdapter == null) {
            return;
        }
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            RecyclerView.ViewHolder holder = listView.getChildViewHolder(child);
            if (holder != null) {
                listAdapter.onBindViewHolder(holder, listView.getChildAdapterPosition(child));
            }
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        CacheControlActivity.canceled = true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(getString(R.string.fluffySettings));
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        actionBar.createMenu().addItem(1000, (Drawable) null).setVisibility(View.INVISIBLE);
        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context) {
            @Override
            public Integer getSelectorColor(int position) {
                return getThemedColor(Theme.key_listSelector);
            }
        };
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setDurations(350);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setSupportsChangeAnimations(false);
        listView.setItemAnimator(itemAnimator);
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == cameraSelectRow) {
                int selected;
                if (fluffyConfig.frontCamera) {
                    selected = 0;
                } else {
                    selected = 1;
                }
                Dialog dlg = AlertsCreator.createSingleChoiceDialog(getParentActivity(), new String[]{
                                getString(R.string.CameraFront),
                                getString(R.string.CameraBack)},
                        getString(R.string.SelectCamera), selected, (dialog, which) -> {
                            if (which == 0) {
                                fluffyConfig.frontCamera = true;
                            } else {
                                fluffyConfig.frontCamera = false;
                            }
                            updateCameraSelect = true;
                            writeCamera();

                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }

                            rebind(cameraSelectRow);
                        });
                setVisibleDialog(dlg);
                dlg.show();
            } else if (position == selectTitleRow) {
                if (getParentActivity() == null) {
                    return;
                }
                AtomicReference<Dialog> dialogRef = new AtomicReference<>();

                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                CharSequence[] items = new CharSequence[]{
                        fluffyConfig.getUsername(),
                        "fluffy",
                        "telegram",
                        "Disable"
                };

                for (int i = 0; i < items.length; ++i) {
                    final int index = i;
                    RadioColorCell cell = new RadioColorCell(getParentActivity());
                    cell.setPadding(dp(4), 0, dp(4), 0);
                    cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
                    cell.setTextAndValue(items[index], index == fluffyConfig.typeTitle);
                    cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ALL));
                    linearLayout.addView(cell);
                    cell.setOnClickListener(v -> {
                        fluffyConfig.setTypeTitle(index);
                        getNotificationCenter().postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);
                        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(selectTitleRow);
                        if (holder != null) {
                            listAdapter.onBindViewHolder(holder, selectTitleRow);
                        }

                        if (LaunchActivity.getSafeLastFragment() != null) {
                            BulletinHelper.showRestartNotification(LaunchActivity.getSafeLastFragment());
                        }
                        getNotificationCenter().postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);
                        dialogRef.get().dismiss();
                    });
                }

                Dialog dialog = new AlertDialog.Builder(getParentActivity())
                        .setTitle(getString(R.string.TitleSelecter))
                        .setView(linearLayout)
                        .setNegativeButton(getString("Cancel", R.string.Cancel), null)
                        .create();
                dialogRef.set(dialog);
                showDialog(dialog);
            } else if (position == localPremiumRow) {
                fluffyConfig.togglePremiumMode();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.premiumMode);
            } else if (position == zodiacShowRow) {
                fluffyConfig.toogleZodiacShow();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.zodiacShow);
            } else if (position == storiesShowRow) {
                fluffyConfig.toggleShowStories();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.showStories);
                getNotificationCenter().postNotificationName(NotificationCenter.storiesEnabledUpdate);
            } else if (position == callShowRow) {
                fluffyConfig.toggleShowCallIcon();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.showCallIcon);
            } else if (position == centerTitleRow) {
                fluffyConfig.toggleCenterTitle();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.centerTitle);
                if (LaunchActivity.getSafeLastFragment() != null) {
                    BulletinHelper.showRestartNotification(LaunchActivity.getSafeLastFragment());
                }
                getNotificationCenter().postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);
            } else if (position == downloadSpeedBoostRow) {
                fluffyConfig.toogleDownloadSpeedBoost();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.downloadSpeedBoost);
            } else if (position == voiceUseCloudflareRow) {
                TextCheckCell textCheckCell = (TextCheckCell) view;
//                textCheckCell.setChecked(fluffyConfig.voiceUseCloudflare);
            } else if (position == cfCredentialsRow) {
                WhisperHelper.showCfCredentialsDialog(this);
            } else if (position == disableRoundRow) {
                fluffyConfig.toogleRoundingNumber();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.disableRoundingNumber);
            } else if (position == systemTypefaceRow) {
                fluffyConfig.toogleUseSystemFonts();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.useSystemFonts);
                AndroidUtilities.clearTypefaceCache();
                if (LaunchActivity.getSafeLastFragment() != null) {
                    BulletinHelper.showRestartNotification(LaunchActivity.getSafeLastFragment());
                }
            } else if (position == formatTimeWithSecondsRow) {
                fluffyConfig.toogleFormatTimeWithSeconds();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.formatTimeWithSeconds);
            } else if (position == useSolarIconsRow) {
                fluffyConfig.toggleUseSolarIcons();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.useSolarIcons);
            } else if (position == moreInfoOnlineRow) {
                fluffyConfig.toggleMoreInfoOnline();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.moreInfoOnline);
            } else if (position == unmuteVideoWithVolumeRow) {
                fluffyConfig.toggleUnmuteVideoWithVolume();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.unmuteVideoWithVolume);
            } else if (position == saveEditRow) {
                fluffyConfig.saveEditSwitch();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.saveEdit);
            } else if (position == saveDelRow) {
                fluffyConfig.saveDelSwitch();
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(fluffyConfig.saveDel);
            } else if (position == doubleTapInActionRow) {
                if (getParentActivity() == null) {
                    return;
                }

                AtomicReference<Dialog> dialogRef = new AtomicReference<>();

                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                CharSequence[] items = new CharSequence[]{
                        "None", // DOUBLE_TAP_ACTION_NONE
                        "Reaction", // DOUBLE_TAP_ACTION_REACTION
                        "Reply", // DOUBLE_TAP_ACTION_REPLY
                        "Save", // DOUBLE_TAP_ACTION_SAVE
                        "Repeat", // DOUBLE_TAP_ACTION_REPEAT
                        "Edit" // DOUBLE_TAP_ACTION_EDIT
                };

                for (int i = 0; i < items.length; ++i) {
                    final int index = i;
                    RadioColorCell cell = new RadioColorCell(getParentActivity());
                    cell.setPadding(dp(4), 0, dp(4), 0);
                    cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
                    cell.setTextAndValue(items[index], index == fluffyConfig.doubleTapInAction); // Проверяем текущее выбранное действие
                    cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ALL));
                    linearLayout.addView(cell);
                    cell.setOnClickListener(v -> {
                        fluffyConfig.setDoubleTapInAction(index);
                        getNotificationCenter().postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);

                        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(doubleTapInActionRow);
                        if (holder != null) {
                            listAdapter.onBindViewHolder(holder, doubleTapInActionRow);
                        }

                        dialogRef.get().dismiss();
                    });
                }

                Dialog dialog = new AlertDialog.Builder(getParentActivity())
                        .setTitle("Select Double Tap Action")
                        .setView(linearLayout)
                        .setNegativeButton("Cancel", null)
                        .create();
                dialogRef.set(dialog);
                showDialog(dialog);
            } else if (position == doubleTapOutActionRow) {
                if (getParentActivity() == null) {
                    return;
                }

                AtomicReference<Dialog> dialogRef = new AtomicReference<>();

                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                CharSequence[] items = new CharSequence[]{
                        "None", // DOUBLE_TAP_ACTION_NONE
                        "Reaction", // DOUBLE_TAP_ACTION_REACTION
                        "Reply", // DOUBLE_TAP_ACTION_REPLY
                        "Save", // DOUBLE_TAP_ACTION_SAVE
                        "Repeat", // DOUBLE_TAP_ACTION_REPEAT
                        "Edit" // DOUBLE_TAP_ACTION_EDIT
                };

                for (int i = 0; i < items.length; ++i) {
                    final int index = i;
                    RadioColorCell cell = new RadioColorCell(getParentActivity());
                    cell.setPadding(dp(4), 0, dp(4), 0);
                    cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
                    cell.setTextAndValue(items[index], index == fluffyConfig.doubleTapOutAction);
                    cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ALL));
                    linearLayout.addView(cell);
                    cell.setOnClickListener(v -> {
                        fluffyConfig.setDoubleTapOutAction(index);
                        getNotificationCenter().postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);

                        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(doubleTapOutActionRow);
                        if (holder != null) {
                            listAdapter.onBindViewHolder(holder, doubleTapOutActionRow);
                        }

                        dialogRef.get().dismiss();
                    });
                }

                Dialog dialog = new AlertDialog.Builder(getParentActivity())
                        .setTitle("Select Double Tap Action")
                        .setView(linearLayout)
                        .setNegativeButton("Cancel", null)
                        .create();
                dialogRef.set(dialog);
                showDialog(dialog);
            }
        });
        return fragmentView;
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        DownloadController.getInstance(currentAccount).checkAutodownloadSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        rebindAll();
        updateRows(false);
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    holder.itemView.setBackgroundDrawable(Theme.getThemedDrawableByKey(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                }
                case 6: {
                    TextCell textCell = (TextCell) holder.itemView;
                    break;
                }
                case 1: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setCanDisable(false);
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == cameraSelectRow) {
                        textCell.setIcon(0);
                        String value = null;
                        if (fluffyConfig.frontCamera) {
                            value = getString(R.string.CameraFront);
                        } else {
                            value = getString(R.string.CameraBack);
                        }
                        textCell.setTextAndValue(getString(R.string.SelectCamera), value, updateCameraSelect, true);
                        updateCameraSelect = false;
                    } else if (position == selectTitleRow) {
                        String value = switch (fluffyConfig.typeTitle) {
                            case 0 -> fluffyConfig.getUsername();
                            case 1 -> "fluffy";
                            case 2 -> "telegram";
                            case 3 -> "Disable";
                            default -> LocaleController.getString(R.string.AppName);
                        };
                        textCell.setTextAndValue(getString(R.string.TitleSelecter), value, false, true);
                    } else if (position == cfCredentialsRow) {
                        textCell.setIcon(0);
                        textCell.setText(getString(R.string.CloudflareCredentials), true);
                    } else if (position == doubleTapInActionRow) {
                        CharSequence[] items = new CharSequence[]{
                                "None", // DOUBLE_TAP_ACTION_NONE (0)
                                "Reaction", // DOUBLE_TAP_ACTION_REACTION (1)
                                "Reply", // DOUBLE_TAP_ACTION_REPLY (3)
                                "Save", // DOUBLE_TAP_ACTION_SAVE (4)
                                "Repeat", // DOUBLE_TAP_ACTION_REPEAT (5)
                                "Edit" // DOUBLE_TAP_ACTION_EDIT (6)
                        };

                        String value;
                        if (fluffyConfig.doubleTapInAction >= 0 && fluffyConfig.doubleTapInAction < items.length) {
                            value = items[fluffyConfig.doubleTapInAction].toString();
                        } else {
                            value = LocaleController.getString(R.string.AppName);
                        }

                        textCell.setTextAndValue(
                                getString("doubleTapInAction", R.string.doubleTapInAction),
                                value, // Значение
                                false, // Показывать разделитель?
                                true // Показывать стрелку?
                        );
                    } else if (position == doubleTapOutActionRow) {
                        CharSequence[] items = new CharSequence[]{
                                "None", // DOUBLE_TAP_ACTION_NONE (0)
                                "Reaction", // DOUBLE_TAP_ACTION_REACTION (1)
                                "Reply", // DOUBLE_TAP_ACTION_REPLY (3)
                                "Save", // DOUBLE_TAP_ACTION_SAVE (4)
                                "Repeat", // DOUBLE_TAP_ACTION_REPEAT (5)
                                "Edit" // DOUBLE_TAP_ACTION_EDIT (6)
                        };

                        String value;
                        if (fluffyConfig.doubleTapOutAction >= 0 && fluffyConfig.doubleTapOutAction < items.length) {
                            value = items[fluffyConfig.doubleTapOutAction].toString();
                        } else {
                            value = LocaleController.getString(R.string.AppName);
                        }

                        textCell.setTextAndValue(
                                getString("doubleTapInAction", R.string.doubleTapOutAction),
                                value, // Значение
                                false, // Показывать разделитель?
                                true // Показывать стрелку?
                        );
                    }
                    break;
                }
                case 2: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == chatSettingsSectionRow) {
                        headerCell.setText(getString(R.string.ChatTweak));
                    } else if (position == otherSettingsSectionRow) {
                        headerCell.setText(getString(R.string.Other));
                    } else if (position == appearanceSettingsSectionRow) {
                        headerCell.setText(getString(R.string.appearanceSettings));
                    }
                    break;
                }
                case 3: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == localPremiumRow) {
                        checkCell.setTextAndCheck(getString(R.string.LocalPremium), fluffyConfig.premiumMode, true);
                    } else if (position == zodiacShowRow) {
                        checkCell.setTextAndCheck(getString(R.string.zodiacShow), fluffyConfig.zodiacShow, true);
                    } else if (position == storiesShowRow) {
                        checkCell.setTextAndCheck(getString(R.string.storiesShower), fluffyConfig.showStories, true);
                    } else if (position == callShowRow) {
                        checkCell.setTextAndCheck(getString(R.string.callShower), fluffyConfig.showCallIcon, true);
                    } else if (position == centerTitleRow) {
                        checkCell.setTextAndCheck(getString(R.string.centerTitle), fluffyConfig.centerTitle, true);
                    } else if (position == downloadSpeedBoostRow) {
                        checkCell.setTextAndCheck(getString(R.string.downloadSpeedBoost), fluffyConfig.downloadSpeedBoost, true);
                    } else if (position == voiceUseCloudflareRow) {
//                        checkCell.setTextAndCheck(getString(R.string.UseCloudflare), fluffyConfig.voiceUseCloudflare, true);
                    } else if (position == disableRoundRow) {
                        checkCell.setTextAndValueAndCheck(getString(R.string.DisableNumberRounding), "4.8K -> 4777", fluffyConfig.disableRoundingNumber, true, true);
                    } else if (position == moreInfoOnlineRow) {
                        checkCell.setTextAndCheck(getString(R.string.ExtendedStatusOnline), fluffyConfig.moreInfoOnline, true);
                    } else if (position == unmuteVideoWithVolumeRow) {
                        checkCell.setTextAndCheck(getString(R.string.unmuteVideoWithVolume), fluffyConfig.unmuteVideoWithVolume, true);
                    }  else if (position == saveDelRow) {
                        checkCell.setTextAndCheck(getString(R.string.saveDelRow), fluffyConfig.saveDel, true);
                    }  else if (position == saveEditRow) {
                        checkCell.setTextAndCheck(getString(R.string.saveEditRow), fluffyConfig.saveEdit, true);
                    } else if (position == systemTypefaceRow) {
                        checkCell.setTextAndCheck(getString(R.string.UseSystemTypeface), fluffyConfig.useSystemFonts, true);
                    } else if (position == useSolarIconsRow) {
                        checkCell.setTextAndCheck(getString(R.string.useSolarIcons), fluffyConfig.useSolarIcons, true);
                    } else if (position == formatTimeWithSecondsRow) {
                        checkCell.setTextAndCheck(getString(R.string.formatTime), fluffyConfig.formatTimeWithSeconds, true);
                    }
                    break;
                }
                case 4: {
                    headerSettingsCell = (headerSettingsCell) holder.itemView;
                    headerSettingsCell.setPadding(0, ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) - AndroidUtilities.dp(40), 0, 0);
                    break;
                }
                case 5: {
                    NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;
                    break;
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            if (viewType == 3) {
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                int position = holder.getAdapterPosition();
                if (position == localPremiumRow) {
                    checkCell.setChecked(fluffyConfig.premiumMode);
                } else if (position == zodiacShowRow) {
                    checkCell.setChecked(fluffyConfig.zodiacShow);
                } else if (position == storiesShowRow) {
                    checkCell.setChecked(fluffyConfig.showStories);
                } else if (position == callShowRow) {
                    checkCell.setChecked(fluffyConfig.showCallIcon);
                } else if (position == centerTitleRow) {
                    checkCell.setChecked(fluffyConfig.centerTitle);
                } else if (position == downloadSpeedBoostRow) {
                    checkCell.setChecked(fluffyConfig.downloadSpeedBoost);
                } else if (position == voiceUseCloudflareRow) {
//                    checkCell.setChecked(fluffyConfig.voiceUseCloudflare);
                } else if (position == disableRoundRow) {
                    checkCell.setChecked(fluffyConfig.disableRoundingNumber);
                } else if (position == moreInfoOnlineRow) {
                    checkCell.setChecked(fluffyConfig.moreInfoOnline);
                } else if (position == unmuteVideoWithVolumeRow) {
                    checkCell.setChecked(fluffyConfig.unmuteVideoWithVolume);
                }  else if (position == saveDelRow) {
                    checkCell.setChecked(fluffyConfig.saveDel);
                }  else if (position == saveEditRow) {
                    checkCell.setChecked(fluffyConfig.saveEdit);
                } else if (position == systemTypefaceRow) {
                    checkCell.setChecked(fluffyConfig.useSystemFonts);
                } else if (position == useSolarIconsRow) {
                    checkCell.setChecked(fluffyConfig.useSolarIcons);
                } else if (position == formatTimeWithSecondsRow) {
                    checkCell.setChecked(fluffyConfig.formatTimeWithSeconds);
                }
            }
        }

        public boolean isRowEnabled(int position) {
            return position == chatSettingsSectionRow ||
                    position == appearanceSettingsSectionRow ||
                    position == otherSettingsSectionRow ||
                    position == storiesShowRow ||
                    position == zodiacShowRow ||
                    position == localPremiumRow ||
                    position == voiceUseCloudflareRow ||
                    position == callShowRow ||
                    position == centerTitleRow ||
                    position == downloadSpeedBoostRow ||
                    position == disableRoundRow ||
                    position == moreInfoOnlineRow ||
                    position == unmuteVideoWithVolumeRow ||
                    position == saveEditRow ||
                    position == saveDelRow ||
                    position == systemTypefaceRow ||
                    position == useSolarIconsRow ||
                    position == formatTimeWithSecondsRow;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isRowEnabled(holder.getAdapterPosition());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new HeaderCell(mContext, 22);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new headerSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new NotificationsCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                default:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == chatSettingsSectionRow || position == appearanceSettingsSectionRow || position == otherSettingsSectionRow) {
                return 2;
            } else if (position == localPremiumRow ||
                    position == storiesShowRow ||
                    position == zodiacShowRow ||
                    position == voiceUseCloudflareRow ||
                    position == callShowRow ||
                    position == disableRoundRow ||
                    position == moreInfoOnlineRow ||
                    position == unmuteVideoWithVolumeRow ||
                    position == centerTitleRow ||
                    position == saveEditRow ||
                    position == saveDelRow ||
                    position == systemTypefaceRow ||
                    position == useSolarIconsRow ||
                    position == formatTimeWithSecondsRow ||
                    position == downloadSpeedBoostRow
            ) {
                return 3;
            } else if (position == aboutFluffyRow) {
                return 4;
            } else {
                return 1;
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        return themeDescriptions;
    }
}
