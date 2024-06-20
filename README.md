# Noteworthy
Noteworthy is a full stack, serverless, web-based note taking app, focusing on user-friendly features like voice notes, which utilize Speech-to-Text AI to enable more efficient and accurate note taking.

<p align="center">
  <img src="https://github.com/citomcclure/noteworthy/assets/38021988/fb0da571-3b26-4371-9ac6-70c253d6ce9b" alt="App Snapshot" width="80%">
</p>

<p align="center">
  Available for use here: https://drh6zqq3rdeze.cloudfront.net/
</p>

## Problem Alignment
There are a myriad of note taking apps on the market, each with their pros and cons. A list of common pain points:
- lack of device-sharing
- bloated with features, often requiring native clients
- require significant organizational overhead (subpaging, keyword tags, markup-only input)
- rely on user's typing speed and motivation

Noteworthy aims to address these pain points with the following goals,
1. Web-based login for portability
2. One-page application with simple and intuitive UI/UX
3. Voice notes, which transcribe audio for creating quick and accurate notes

The motivation behind Voice Notes was to reduce the obvious barrier to note taking - typing. Several user groups can benefit, such as busy employees who do not have time to take notes between meetings, people with phsyical disabilities, and slower typers. From a product perspective, productivity apps necessitate strong engagement and retention metrics. Reducing the barrier to take notes using Voice should increase product metrics like
1. Number of notes (avg # notes = total # notes / active users)
2. Note length (avg note length = total # characters / (total notes * active users))

ultimately creating the core product loop: creating more note data <-> accessing more note data



## Features
- Login/Logout
- Onboarding
- Basics: View, Create, Update, and Delete Notes
- Sort by Date
- Autosave
- Voice Notes

See project board for upcoming features and known issues: https://github.com/users/citomcclure/projects/1/views/1 

<p align="center">
  <img src="https://github.com/citomcclure/noteworthy/assets/38021988/4b5eefbf-3588-4e42-baa5-34241b9400b4" alt="Project Board Snapshot" width="80%">
</p>


## Architecture Overview
### Frontend
The frontend uses HTML, CSS, JavaScript, and Bootstrap.

Major components used by Bootstrap include grid layout, drop down menu for sort, and spinner animations for autosaving/deleting states, but otherwise the design was made through extensive use of CSS styling.

In order to maintain a single page application, API endpoints are optimized to reduce backend calls and maintain concurrent state in JS Datastore. Uses Axios API to make HTTPS requests to two REST endpoints:
 - `/notes` with GET, POST, PUT, and DELETE HTTP methods
 - `/notes/voice` with POST HTTP method

IAM is handled by Amazon Cognito for user authentication. The web app is served through an Amazon CloudFront distribution.

### Backend
The backend is written in Java and leverages a serverless application model (SAM) using Lambda, in conjunction with several other AWS services.

The entire application is configured using a CloudFormation template to deploy resources, manage access through policies, and other configurations. The template also informs API Gateway which endpoints and HTTP methods correspond with which Lambda. Once a Lambda is triggered, the same general flow of information is executed for all Lambdas:
1. A _Request_ object is created using a Builder with any data from the client request (e.g., email)
2. An _Activity_ object applies the business logic using the _Request_ object
3. Data is saved/loaded using _DAOs_ (Data Access Objects) corresponding to each DynamoDB table
4. A _Result_ object is created using a Builder and returned back to the _Lambda_
5. Finally a response is generated and returned to the client

There are two DynamoDB tables, with the following schema:

<p align="center">
  <img src="https://github.com/citomcclure/noteworthy/assets/38021988/8e9bdf81-968c-46dc-90db-8a6e09267b45" alt="DB Schema" width="80%">
</p>

Other:
- Gradle for build automation and dependency management
- Dagger for Dependency Injection
- JUnit framework for testing
- Mockito for mocking
- Jackson library for serialization

### Voice Note
The Voice Note capability has a more complex end-to-end implementation.

<p align="center">
  <img src="https://github.com/citomcclure/noteworthy/assets/38021988/e22c7f42-f923-47f1-a7b4-38490dded9b6" alt="Voice Note Snapshot" width="80%">
</p>

On the frontend, the user's audio is captured using the browser's media device as a stream. Using a third party library ([extendable-media-recorder]([url](https://github.com/chrisguttandin/extendable-media-recorder?tab=readme-ov-file)) + [extendable-media-recorder-wav-encoder]([url](https://github.com/chrisguttandin/extendable-media-recorder-wav-encoder?tab=readme-ov-file)) under the MIT license), a media recorder is set up using the stream and audio/wav MIME type. Although the default `.webm` format could be used, the preferred format for Amazon Transcribe is WAV with PCM 16-bit encoding. Via a POST call to `/notes/voice`, the WAV file is included as form data in a `Content-Type: multipart/form-data` HTTP request.

On the backend, API Gateway Base64 encodes the request, which is parsed to remove the non-WAV elements that are prepended to the request body. The Request object is built using the user's email and the audio as an array of bytes. The business logic leverages wrapper classes for the Amazon S3 and Amazon Transcribe services to help abstract a lot of non-business logic out of the _Activity_ class. Together they acheive the following:
- Validate audio is WAV format and detect sample rate
- Create .wav file, save to temp (in Lambda this is the execution environment), and store the file in Amazon S3
- Create and start a transcription batch job in Amazon Transcribe
- Poll transcription job until it has completed (or failed)
- Retrieve json results from S3 and parse
- Create `Transcription` object and save to `transcriptions` table in DDB
- Create new voice note (`Note` object) using transcript and save to `notes` table in DDB

Note: Because AWS SDK 1.x for Java is used across the project, there were several limitations such as not being able to stream transcription results (instead of batch). Planned optimizations and spike tickets for improving the voice note feature can be found on the project board (e.g., using presigned URLs): https://github.com/users/citomcclure/projects/1


## Earlier Work
Original designs can be found here: https://miro.com/app/board/uXjVKGfpUwM=/?share_link_id=519475842006

<p align="center">
  <img src="https://github.com/citomcclure/noteworthy/assets/38021988/a47d5976-b9e4-489b-8b48-ba8a2ed94d86" alt="Voice Note Snapshot" width="80%">
</p>
