package org.totschnig.myexpenses.dialog;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.QifImport;
import org.totschnig.myexpenses.export.qif.QifDateFormat;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.provider.TransactionProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class QifImportDialogFragment extends ImportSourceDialogFragment implements
    LoaderManager.LoaderCallbacks<Cursor>, OnItemSelectedListener {
  Spinner mAccountSpinner, mDateFormatSpinner, mCurrencySpinner;
  private SimpleCursorAdapter mAccountsAdapter;

  static final String PREFKEY_IMPORT_QIF_DATE_FORMAT = "import_qif_date_format";
  static final String PREFKEY_IMPORT_QIF_FILE_URI = "import_qif_file_uri";
  private MergeCursor mAccountsCursor;

  public static final QifImportDialogFragment newInstance() {
    return new QifImportDialogFragment();
  }
  @Override
  protected int getLayoutId() {
    return R.layout.qif_import_dialog;
  }
  @Override
  protected int getLayoutTitle() {
    return R.string.pref_import_qif_title;
  }

  @Override
  String getTypeName() {
    return "QIF";
  }

  @Override
  public void onClick(DialogInterface dialog, int id) {
    if (id == AlertDialog.BUTTON_POSITIVE) {
      QifDateFormat format = (QifDateFormat) mDateFormatSpinner.getSelectedItem();
      MyApplication.getInstance().getSettings().edit()
        .putString(PREFKEY_IMPORT_QIF_FILE_URI, mUri.toString())
        .putString(PREFKEY_IMPORT_QIF_DATE_FORMAT, format.toString())
        .commit();
      ((QifImport) getActivity()).onSourceSelected(
          mUri,
          format,
          mAccountSpinner.getSelectedItemId(),
          ((Account.CurrencyEnum) mCurrencySpinner.getSelectedItem()).name(),
          mImportTransactions.isChecked(),
          mImportCategories.isChecked(),
          mImportParties.isChecked()
          );
    } else {
      super.onClick(dialog, id);
    }
  }
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    CursorLoader cursorLoader = new CursorLoader(
        getActivity(),
        TransactionProvider.ACCOUNTS_BASE_URI,
        new String[] {
          KEY_ROWID,
          KEY_LABEL,
          KEY_CURRENCY},
        null,null, null);
    return cursorLoader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    MatrixCursor extras = new MatrixCursor(new String[] {
        KEY_ROWID,
        KEY_LABEL,
        KEY_CURRENCY
    });
    extras.addRow(new String[] {
        "0",
        getString(R.string.menu_create_account),
        Account.getLocaleCurrency().getCurrencyCode()
    });
    mAccountsCursor = new MergeCursor(new Cursor[] {extras,data});
    mAccountsAdapter.swapCursor(mAccountsCursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mAccountsCursor = null;
    mAccountsAdapter.swapCursor(null);
  }

  @Override
  protected void setupDialogView(View view) {
    super.setupDialogView(view);
    mAccountSpinner = (Spinner) view.findViewById(R.id.Account);
    mAccountsAdapter = new SimpleCursorAdapter(wrappedCtx, android.R.layout.simple_spinner_item, null,
        new String[] {KEY_LABEL}, new int[] {android.R.id.text1}, 0);
    mAccountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mAccountSpinner.setAdapter(mAccountsAdapter);
    mAccountSpinner.setOnItemSelectedListener(this);
    getLoaderManager().initLoader(0, null, this);
    mDateFormatSpinner = (Spinner) view.findViewById(R.id.DateFormat);
    ArrayAdapter<QifDateFormat> dateFormatAdapter =
        new ArrayAdapter<QifDateFormat>(
            wrappedCtx, android.R.layout.simple_spinner_item, QifDateFormat.values());
    dateFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mDateFormatSpinner.setAdapter(dateFormatAdapter);
    mDateFormatSpinner.setSelection(
        QifDateFormat.valueOf(
            MyApplication.getInstance().getSettings()
            .getString(PREFKEY_IMPORT_QIF_DATE_FORMAT, "EU")
            == "EU" ? "EU" : "US")
        .ordinal());
    mCurrencySpinner = (Spinner) view.findViewById(R.id.Currency);
    ArrayAdapter<Account.CurrencyEnum> curAdapter = new ArrayAdapter<Account.CurrencyEnum>(
        wrappedCtx, android.R.layout.simple_spinner_item, android.R.id.text1,Account.CurrencyEnum.values());
    curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mCurrencySpinner.setAdapter(curAdapter);
//    mCurrencySpinner.setSelection(
//        Account.CurrencyEnum
//        .valueOf(Account.getLocaleCurrency().getCurrencyCode())
//        .ordinal());
  }
  @Override
  public void onStart() {
    if (mUri==null) {
      String storedUri = MyApplication.getInstance().getSettings()
          .getString(PREFKEY_IMPORT_QIF_FILE_URI, "");
      if (!storedUri.equals("")) {
        mUri = Uri.parse(storedUri);
        try {
          mFilename.setText(getDisplayName(mUri));
        } catch (SecurityException e) {
          // on Kitkat getDisplayname might fail if app is restarted after reboot
          mUri = null;
        }
      }
      super.onStart();
    }
  }
  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position,
      long id) {
    if (mAccountsCursor != null) {
      mAccountsCursor.moveToPosition(position);
      mCurrencySpinner.setSelection(
          Account.CurrencyEnum
          .valueOf(
              mAccountsCursor.getString(2))//2=KEY_CURRENCY
          .ordinal());
      mCurrencySpinner.setEnabled(position==0);
    }
  }
  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }
}
