var summationRule = {
	id: 1,
	type: "summation",
	optionIds: [1, 5, 23, 54, 75, 99, 120],
	lowerBound: 0,
	upperBound: 5
};

var cumulationRule = {
	id: 1,
	type: "cumulation",
	optionIds: [1, 5, 23, 54, 75, 99, 120],
	lowerBound: 0,
	upperBound: 2
};

var listElectionIssue = {
	id: 1,
	type: "listElection",
	title: {
		de: "Wahlen des SUB-StudentInnenrates 2015",
		fr: "Élection du conseil des étudiant-e-s SUB 2015",
		en: "SUB Elections 2015"
	},
	"description": {
		"de": "Der Wahlzettel kann einer Liste zugeordnet werden und maximal 40 Kandidierende aus verschiedenen Listen enthalten. Einzelne Kandidierende können bis zu dreimal aufgeführt werden. Enthält ein Wahlzettel weniger als 40 Kandidierende, so zählen die fehlenden Einträge als Zusatzstimmen für die ausgewählte Liste. Wenn keine Liste angegeben ist, verfallen diese Stimmen.",
		"fr": "Le bulletin de vote peut comporter au maximum 40 candidats. Il peut comporter des candidats de listes différentes. Chaque électeur peut cumuler jusqu’à trois suffrages sur un candidat. Le bulletin de vote peut-être attribué à une liste. Si un bulletin de vote contient un nombre de candidats inférieurs à 40, les lignes laissées en blanc sont considérées comme autant de suffrages complémentaires attribués à la liste choisie. Si aucune liste n’est indiquée, ces suffrages  sont considérés comme périmés.",
		"en": "Your ballot paper can include up to 40 candidates from all lists. It can include candidates from different lists. You may vote for each candidate up to three times. You can assign a list to your ballot. If you vote for less than 40 candidates, the missing entries will count as list votes for the chosen list. If you do not assign a list, they will expire and count as empty votes."
	},
	"options": [
		{
			"id": 5,
			"type": "list",
			"number": "2",
			"listName": "Junge Grüne Uni Bern",
			"partyName": "jg"
		},
		{
			"id": 6,
			"type": "candidate",
			"number": "2.1",
			"lastName": "Oppold",
			"firstName": "Malvin",
			"sex": "M",
			"studyBranch": "Politikwissenschaft",
			"studyDegree": "MA",
			"studySemester": 5,
			"status": "OLD",
			"listId": 5,
			"listPositions": [1, 2]
		},
		{
			"id": 1,
			"type": "list",
			"number": "1",
			"listName": "Tuxpartei",
			"partyName": "tux"
		},
		{
			"id": 2,
			"type": "candidate",
			"number": "1.1",
			"lastName": "Vuilleumier",
			"firstName": "Sebastian",
			"sex": "M",
			"yearOfBirth": 1988,
			"studyBranch": "Humanmedizin",
			"studyDegree": "MA",
			"studySemester": 10,
			"status": "OLD",
			"listId": 1,
			"listPositions": [1, 2]
		},
		{
			"id": 3,
			"type": "candidate",
			"number": "1.2",
			"lastName": "Schmid",
			"firstName": "Luca",
			"sex": "M",
			"yearOfBirth": 1993,
			"studyBranch": "Humanmedizin",
			"studyDegree": "BA",
			"studySemester": 2,
			"listId": 1,
			"listPositions": [3, 4]
		}
	],
	"rules": [
		{
			"id": 1,
			"type": "summation",
			"optionIds": [1, 5],
			"lowerBound": 0,
			"upperBound": 1
		},
		{
			"id": 2,
			"type": "cumulation",
			"optionIds": [1, 5],
			"lowerBound": 0,
			"upperBound": 1
		},
		{
			"id": 3,
			"type": "summation",
			"optionIds": [2, 3, 4, 65],
			"lowerBound": 0,
			"upperBound": 9
		},
		{
			"id": 4,
			"type": "cumulation",
			"optionIds": [2, 3, 4, 6],
			"lowerBound": 0,
			"upperBound": 2
		}
	]
};




describe('SummationRuleTest', function () {
	it('creating and verifying 1', function () {
		var rule = new SummationRule(summationRule);

		var map = [];
		map[1] = 1;
		map[23] = 2;

		var v = rule.verify(map);
		expect(v).toEqual(true);
	});


	it('creating and verifying 2', function () {
		var rule = new SummationRule(summationRule);

		var map = [];
		map[1] = 2;
		map[23] = 4;

		var v = rule.verify(map);
		expect(v).toEqual(false);
	});

	it('containing option', function () {
		var rule = new SummationRule(summationRule);

		expect(rule.containsOption(1)).toEqual(true);
		expect(rule.containsOption(54)).toEqual(true);
		expect(rule.containsOption("1")).toEqual(false);
		expect(rule.containsOption(7)).toEqual(false);
	});
});


describe('CumulationRuleTest', function () {
	it('creating and verifying 1', function () {
		var rule = new CumulationRule(cumulationRule);

		var map = [];
		map[1] = 1;
		map[23] = 2;

		var v = rule.verify(map);
		expect(v).toEqual(true);
	});


	it('creating and verifying (upperBound) 2', function () {
		var rule = new CumulationRule(cumulationRule);

		var map = [];
		map[1] = 2;
		map[23] = 4;

		var v = rule.verify(map);
		expect(v).toEqual(false);
	});

	it('creating and verifying (lowerBound) 3', function () {
		var rule = new CumulationRule({
			id: 1,
			type: "cumulation",
			optionIds: [1, 5, 23, 54],
			lowerBound: 1,
			upperBound: 3
		});

		var map = [];
		map[1] = 2;
		map[5] = 2;
		map[23] = 3;


		var v = rule.verify(map);
		expect(v).toEqual(false);
	});

	it('containing option', function () {
		var rule = new CumulationRule({
			id: 1,
			type: "cumulation",
			optionIds: [1, 5, 23, 54, 75, 99, 120],
			lowerBound: 0,
			upperBound: 5
		});

		expect(rule.containsOption(1)).toEqual(true);
		expect(rule.containsOption(54)).toEqual(true);
		expect(rule.containsOption("1")).toEqual(false);
		expect(rule.containsOption(7)).toEqual(false);
	});

});


describe('IssueTest', function () {
	it('creating and verifying vote', function () {
		var issue = new Issue(listElectionIssue);

		issue.addChoice(1, 1);
		issue.addChoice(2, 2);

		var v = issue.verifyVote();
		expect(v).toEqual(true);

		issue.addChoice(3, 10);
		v = issue.verifyVote();
		expect(v).toEqual(false);

		issue.removeChoice(3);
		v = issue.verifyVote();
		expect(v).toEqual(true);
	});


});


describe('ListElectionIssueTest', function () {
	it('creating and retreiving lists and listCandidates', function () {
		var issue = Issue.createIssue(listElectionIssue);

		var lists = issue.getLists();
		expect(lists[0].id).toEqual(1);
		expect(lists[1].id).toEqual(5);

		var candidates = issue.getListCandidates(1);
		expect(candidates[1].id).toEqual(2);
		expect(candidates[2].id).toEqual(2);
		expect(candidates[3].id).toEqual(3);
		expect(candidates[4].id).toEqual(3);

	});


});




