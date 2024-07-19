import React from 'react';
import './Modal.css'; // Ensure to create and import a CSS file for modal styling

const Modal = ({ show, handleClose, children }) => {
    if (!show) return null;

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <button className="close-button" onClick={handleClose}>X</button>
                {children}
            </div>
        </div>
    );
};

export default Modal;
