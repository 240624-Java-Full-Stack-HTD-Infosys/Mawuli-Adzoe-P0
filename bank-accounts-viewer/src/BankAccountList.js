import React from 'react';
import BankAccountCard from './BankAccountCard';

const BankAccountList = ({ accounts }) => (
    <div className="bank-accounts">
        <h3>Bank Accounts:</h3>
        {accounts.map(account => (
            <BankAccountCard key={account.accountNumber} account={account} />
        ))}
    </div>
);

export default BankAccountList;
