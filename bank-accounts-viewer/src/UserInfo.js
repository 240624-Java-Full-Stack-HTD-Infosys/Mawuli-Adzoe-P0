import React from 'react';

const UserInfo = ({ user }) => (
    <div className="user-info">
        <p><strong>Name:</strong> {user.name}</p>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>Phone:</strong> {user.phone}</p>
        <p><strong>Admin:</strong> {user.isAdmin ? "Yes" : "No"}</p>
    </div>
);

export default UserInfo;
