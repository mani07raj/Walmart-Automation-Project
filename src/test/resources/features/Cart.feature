@regression @cart
Feature: Add Product to Cart from Departments
  As a Walmart customer
  I want to add a product to my cart via Departments navigation
  So that I can purchase it later

  Background:
    Given I am on the Walmart homepage

  @smoke
  Scenario: Add a Toys product to cart and validate cart details
    When I click on the Departments menu
    And I hover over "Toys & Outdoor Play"
    And I click on "All Toys & Outdoor Play"
    And I select the first product from the listing
    Then I should be on the product details page
    When I click on "Add to cart"
    Then the product should be added to the cart
    And the cart subtotal should be displayed
    And the estimated total should be displayed
    And the cart icon count should be "1"
