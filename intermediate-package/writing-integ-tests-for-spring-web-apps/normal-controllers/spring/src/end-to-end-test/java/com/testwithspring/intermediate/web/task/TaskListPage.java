package com.testwithspring.intermediate.web.task;

import com.testwithspring.intermediate.WebDriverUrlBuilder;
import org.openqa.selenium.WebDriver;

/**
 * This page object is use dto interact with the task list page.
 */
public final class TaskListPage {

    private final WebDriver browser;

    public TaskListPage(WebDriver browser) {
        this.browser = browser;
    }

    /**
     * Returns the url address of the task list page.
     * @return
     */
    public String getPageUrl() {
        return WebDriverUrlBuilder.buildFromRelativeUrl("/");
    }
}