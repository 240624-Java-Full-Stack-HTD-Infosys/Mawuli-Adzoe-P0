import React, { useState, useContext } from 'react';
import UserContext from './UserProvider';

const TransactionForm = ({ handleClose }) => {
    const { deposit, withdraw, transfer } = useContext(UserContext);
    const [transactionType, setTransactionType] = useState('deposit');
    const [formData, setFormData] = useState({ accountNumber: '', amount: '', toAccountNumber: '' });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        switch (transactionType) {
            case 'deposit':
                deposit(formData.accountNumber, formData.amount);
                break;
            case 'withdraw':
                withdraw(formData.accountNumber, formData.amount);
                break;
            case 'transfer':
                transfer(formData.accountNumber, formData);
                break;
            default:
                break;
        }
        handleClose();
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>Create Transaction</h3>
            <label>
                Transaction Type:
                <select name="transactionType" value={transactionType} onChange={(e) => setTransactionType(e.target.value)}>
                    <option value="deposit">Deposit</option>
                    <option value="withdraw">Withdraw</option>
                    <option value="transfer">Transfer</option>
                </select>
            </label>
            <label>
                Account Number:
                <input type="text" name="accountNumber" value={formData.accountNumber} onChange={handleChange} />
            </label>
            <label>
                Amount:
                <input type="number" name="amount" value={formData.amount} onChange={handleChange} />
            </label>
            {transactionType === 'transfer' && (
                <label>
                    To Account Number:
                    <input type="text" name="toAccountNumber" value={formData.toAccountNumber} onChange={handleChange} />
                </label>
            )}
            <button type="submit">Submit</button>
            <button type="button" onClick={handleClose}>Cancel</button>
        </form>
    );
};

export default TransactionForm;
