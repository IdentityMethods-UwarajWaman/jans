Feature: Uma Scopes

Scenario: Fetch all uma scopes without bearer token
Given url umascopes_url
When method GET
Then status 401

Scenario: Fetch all uma scopes
Given url umascopes_url
And  header Authorization = 'Bearer ' + accessToken
When method GET
Then status 200
And assert response.length != null

Scenario: Fetch the first three uma scopes
Given url umascopes_url
And  header Authorization = 'Bearer ' + accessToken
And param limit = 3
When method GET
Then status 200
And assert response.length == 3

Scenario: Search uma scopes given a search pattern
Given url umascopes_url
And  header Authorization = 'Bearer ' + accessToken
And param pattern = 'SCIM Access'
When method GET
Then status 200
And assert response.length == 1

@CreateUpdateDelete
Scenario: Create new Uma Scope
Given url umascopes_url
And header Authorization = 'Bearer ' + accessToken
And request read('classpath:uma-scope.json')
When method POST
Then status 201
Then def result = response
Then set result.displayName = 'UpdatedQAAddedUmaScope'
Then def inum_before = result.inum
Given url umascopes_url
And header Authorization = 'Bearer ' + accessToken
And request result
When method PUT
Then status 200
And assert response.displayName == 'UpdatedQAAddedUmaScope'
And assert response.inum == inum_before
Given url umascopes_url + '/' +response.inum
And header Authorization = 'Bearer ' + accessToken
When method DELETE
Then status 204

Scenario: Delete a non-existion uma scope by inum
Given url umascopes_url + '/1402.66633-8675-473e-a749'
And header Authorization = 'Bearer ' + accessToken
When method GET
Then status 404


Scenario: Get an uma scope by inum(unexisting scope)
Given url openidscopes_url + '/53553532727272772'
And header Authorization = 'Bearer ' + accessToken
When method GET
Then status 404

Scenario: Get an uma scope by inum
Given url umascopes_url
And header Authorization = 'Bearer ' + accessToken
When method GET
Then status 200
Given url umascopes_url + '/' +response[0].inum
And header Authorization = 'Bearer ' + accessToken
When method GET
Then status 200