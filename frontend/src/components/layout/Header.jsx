import React from "react";

export function Header({ currentPageTitle }) {
  return (
    <header className="top-header">
      <div className="header-title">{currentPageTitle}</div>
      <div className="header-right">
        <div className="system-tag" title="Backend connected">
          <span className="status-dot" aria-hidden="true" />
          <span>Operations Online</span>
        </div>
      </div>
    </header>
  );
}
