/*   This file is part of My Expenses.
 *   My Expenses is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   My Expenses is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with My Expenses.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.totschnig.myexpenses.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import static org.totschnig.myexpenses.provider.DatabaseConstants.*;

/**
 * a transfer consists of a pair of transactions, one for each account
 * this class handles creation and update
 * @author Michael Totschnig
 *
 */
public class Transfer extends Transaction {
  
  public Transfer(long accountId,long amount) {
    super(accountId,amount);
  }
  public Transfer(long accountId, Money amount) {
    super(accountId,amount);
  }

  /* (non-Javadoc)
   * @see org.totschnig.myexpenses.Transaction#save()
   */
  public Uri save() {
    Uri uri;
    long amount = this.amount.getAmountMinor();
    //the id of the peer_account is stored in KEY_TRANSFER_ACCOUNT,
    //the id of the peer transaction is stored in KEY_TRANSFER_PEER
    ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_COMMENT, comment);
    initialValues.put(KEY_DATE, dateAsString);
    initialValues.put(KEY_AMOUNT, amount);
    initialValues.put(KEY_TRANSFER_ACCOUNT, transfer_account);
    if (id == 0) {
      initialValues.put(KEY_ACCOUNTID, accountId);
      uri = cr().insert(CONTENT_URI, initialValues);
      id = ContentUris.parseId(uri);
      initialValues.put(KEY_AMOUNT, 0 - amount);
      initialValues.put(KEY_TRANSFER_ACCOUNT, accountId);
      initialValues.put(KEY_ACCOUNTID, transfer_account);
      initialValues.put(KEY_TRANSFER_PEER,id);
      Uri transferUri = cr().insert(CONTENT_URI, initialValues);
      transfer_peer = ContentUris.parseId(transferUri);
      //we have to set the transfer_peer for the first transaction
      ContentValues args = new ContentValues();
      args.put(KEY_TRANSFER_PEER,transfer_peer);
      cr().update(Uri.parse(CONTENT_URI+ "/" + id), args, null, null);
    } else {
      uri = Uri.parse(CONTENT_URI + "/" + id);
      cr().update(uri,initialValues,null,null);
      initialValues.put(KEY_AMOUNT, 0 - amount);
      //if the user has changed the account to which we should transfer,
      //in the peer transaction we need to update the account_id
      initialValues.put(KEY_ACCOUNTID, transfer_account);
      //the account from which is transfered is not altered
      initialValues.remove(KEY_TRANSFER_ACCOUNT);
      cr().update(Uri.parse(CONTENT_URI + "/" + transfer_peer),initialValues,null,null);
    }
    return uri;
  }
}
