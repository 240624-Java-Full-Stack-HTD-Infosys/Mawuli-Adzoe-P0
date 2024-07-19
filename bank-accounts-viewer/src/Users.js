import React, { useState, useContext } from 'react';
import UserContext, { UserProvider } from './UserProvider';
import './Users.css';
import UserCreationForm from './UserCreationForm';
import BankAccountCreationForm from './BankAccountCreationForm';
import TransactionForm from './TransactionForm';
import Modal from './Modal';
import UserCard from './UserCard';
import Button from './Button';

const Users = () => {
    const [showUserForm, setShowUserForm] = useState(false);
    const [showBankAccountForm, setShowBankAccountForm] = useState(false);
    const [showTransactionForm, setShowTransactionForm] = useState(false);

    return (
        <UserProvider>
            <div className="user-container">
                <h2>Users</h2>
                <ActionButtons
                    setShowUserForm={setShowUserForm}
                    setShowBankAccountForm={setShowBankAccountForm}
                    setShowTransactionForm={setShowTransactionForm}
                />
                <Modal show={showUserForm} handleClose={() => setShowUserForm(false)}>
                    <UserCreationForm handleClose={() => setShowUserForm(false)} />
                </Modal>
                <Modal show={showBankAccountForm} handleClose={() => setShowBankAccountForm(false)}>
                    <BankAccountCreationForm handleClose={() => setShowBankAccountForm(false)} />
                </Modal>
                <Modal show={showTransactionForm} handleClose={() => setShowTransactionForm(false)}>
                    <TransactionForm handleClose={() => setShowTransactionForm(false)} />
                </Modal>
                <UserList />
            </div>
        </UserProvider>
    );
};

const ActionButtons = ({ setShowUserForm, setShowBankAccountForm, setShowTransactionForm }) => (
    <div className="action-buttons">
        <Button className="create-user" onClick={() => setShowUserForm(true)}>Create User</Button>
        <Button className="create-bank-account" onClick={() => setShowBankAccountForm(true)}>Create Bank Account</Button>
        <Button className="create-transaction" onClick={() => setShowTransactionForm(true)}>Create Transaction</Button>
    </div>
);

const UserList = () => {
    const { users, loading, error, message } = useContext(UserContext);

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div className="user-container">
            {message && <div className="message">{message}</div>}
            {error && <div className="error">Error: {error}</div>}
            <div className="card-grid">
                {users.map(user => (
                    <UserCard key={user.userId} user={user} />
                ))}
            </div>
        </div>
    );
};

export default Users;
