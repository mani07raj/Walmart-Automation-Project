@regression @search
Feature: Walmart Product Search
  As a Walmart customer
  I want to search for products using the search bar
  So that I can find relevant products quickly

  Background:
    Given I am on the Walmart homepage

  @smoke
  Scenario: Search for iPhone using the search bar
    When I type "iPhone" in the search bar
    And I click on the first search suggestion
    Then the search results page should be displayed
    And the search results should be relevant to "iPhone"

  Scenario Outline: Search for multiple products
    When I type "<product>" in the search bar
    And I click on the first search suggestion
    Then the search results page should be displayed
    And the search results should be relevant to "<product>"

    Examples:
      | product    |
      | Samsung    |
      | MacBook    |
      | Headphones |
