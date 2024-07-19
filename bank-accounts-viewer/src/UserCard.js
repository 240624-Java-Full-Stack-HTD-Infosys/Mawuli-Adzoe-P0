import React, { useState } from 'react';
import UserInfo from './UserInfo';
import EditUserForm from './EditUserForm';
import BankAccountList from './BankAccountList';
import Button from './Button';

const UserCard = ({ user }) => {
    const [editing, setEditing] = useState(false);

    return (
        <div className="user-card">
            <UserInfo user={user} />
            <Button className="edit-user" onClick={() => setEditing(true)}>Edit User</Button>
            {editing && <EditUserForm user={user} setEditing={setEditing} />}
            {user.bankAccounts.length > 0 ? (
                <BankAccountList accounts={user.bankAccounts} />
            ) : (
                <p>No Bank Accounts</p>
            )}
        </div>
    );
};

export default UserCard;
