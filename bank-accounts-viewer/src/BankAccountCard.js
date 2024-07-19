import React, { useState, useContext, useEffect } from 'react';
import axios from 'axios';
import UserContext from './UserProvider';
import Button from './Button';
import './Transactions.css'; // Import the new CSS file

const BankAccountCard = ({ account }) => {
    const { deleteAccount } = useContext(UserContext);
    const [transactions, setTransactions] = useState([]);
    const [showTransactions, setShowTransactions] = useState(false);

    const loadTransactions = async () => {
        try {
            const response = await axios.get(`http://localhost:7000/account/${account.accountNumber}/transactions`, {
                withCredentials: true,
            });
            setTransactions(response.data);
        } catch (error) {
            console.error("Error loading transactions:", error);
        }
    };

    const toggleTransactions = () => {
        if (!showTransactions && transactions.length === 0) {
            loadTransactions();
        }
        setShowTransactions(!showTransactions);
    };

    useEffect(() => {
        if (transactions.length > 0) {
            console.log("Transactions:", transactions);
        }
    }, [transactions]);

    return (
        <div className="account-card">
            <p><strong>Account Number:</strong> {account.accountNumber}</p>
            <p><strong>Account Type:</strong> {account.accountType}</p>
            <p><strong>Balance:</strong> {account.balance.toFixed(2)}</p>
            <Button className="delete-account" onClick={() => deleteAccount(account.accountNumber)}>Delete Account</Button>
            <Button className="view-transactions" onClick={toggleTransactions}>
                {showTransactions ? "Hide Transactions" : "View Transactions"}
            </Button>
            {showTransactions && transactions.length > 0 && (
                <div className="transactions">
                    <h4>Transactions:</h4>
                    {transactions.map(transaction => (
                        <div
                            className={`transaction-card ${transaction.transactionType.toLowerCase()}`}
                            key={transaction.transactionId}
                        >
                            <p><strong>Transaction ID:</strong> {transaction.transactionId}</p>
                            <p><strong>Type:</strong> {transaction.transactionType}</p>
                            <p><strong>Amount:</strong> {transaction.amount.toFixed(2)}</p>
                            <p><strong>Date:</strong> {new Date(transaction.transactionDateTime).toLocaleString()}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default BankAccountCard;
