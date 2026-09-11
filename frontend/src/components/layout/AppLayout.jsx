import React from "react";
import { Sidebar } from "./Sidebar";
import { Header } from "./Header";

export function AppLayout({ currentTab, onSelectTab, pageTitle, children }) {
  return (
    <div className="app-container">
      <Sidebar currentTab={currentTab} onSelectTab={onSelectTab} />
      <div className="main-wrapper">
        <Header currentPageTitle={pageTitle} />
        <main className="content-container">{children}</main>
      </div>
    </div>
  );
}
