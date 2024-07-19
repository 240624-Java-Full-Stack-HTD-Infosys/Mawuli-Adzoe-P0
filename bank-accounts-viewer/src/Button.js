import React from 'react';
import './Button.css';

const Button = ({ className, onClick, children, disabled }) => (
    <button className={`button ${className}`} onClick={onClick} disabled={disabled}>
        {children}
    </button>
);

export default Button;
