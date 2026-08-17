This application is a web based search engine, intended to match people who want to participate in partner dances like slow walz, rumba, salsa, etc.

# Functionality

The core component of this application there is the SearchService, which tries to match searchable items (dancers) to search parameters.
In order to do this, each search parameter needs to have a counterpart in the searchable items.
For example: if you want to search on:
- the distance two dancers are apart, you need to know where they live.
- age difference, you need to know the year of birth.
- how often then want to dance per week, you need to know the prefered frequency.
- dance style and their roles (lead or follow) and skill in them, you need to know what they can do.

A dancers basically is an implementation of both searchable item and search parameters, 
plus some additional descriptive information like a personal text and photos.
Dancer is the only class that is a searchable item, so there is no need to explicitly use that interface.
There are more instances serving as a search parameter:
- a dancer, this allows for batch searching a notification emails.
- anonymous search, for one someone has not registered yet and some fields are unknown (and have generous defaults).
- full search, a logged in dancer is searching, but is fiddling with the search parameters without updating his profile.

Basically what happens in full search is that it is populated from the dancer's profile and he/she can fiddle with them.

# Architecture

This application uses a loosly based domain driven design (DDD) architecture.
It has the following main layers:
- **Domain layer**: contains the core business logic and domain entities like `Dancer`, along with the interfaces for searchable items and search parameters. This layer is independent of any infrastructure concerns.
- **Application layer**: contains the `SearchService` and orchestrates use cases, coordinating between the domain and infrastructure.
- **Infrastructure layer**: external facing logic, like the Vaadin based web application.