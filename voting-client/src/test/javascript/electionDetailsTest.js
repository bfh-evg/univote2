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

var listElectionDetails = {
	"options": [
		{
			"id": 5,
			"type": "listOption",
			"number": "2",
			"listName": "Junge Grüne Uni Bern",
			"partyName": "jg",
			"candidateIds": [6, 6]
		},
		{
			"id": 6,
			"type": "candidateOption",
			"number": "2.1",
			"lastName": "Oppold",
			"firstName": "Malvin",
			"sex": "M",
			"studyBranch": "Politikwissenschaft",
			"studyDegree": "MA",
			"studySemester": 5,
			"status": "OLD"
		},
		{
			"id": 1,
			"type": "listOption",
			"number": "1",
			"listName": "Tuxpartei",
			"partyName": "tux",
			"candidateIds": [2, 2, 3]
		},
		{
			"id": 2,
			"type": "candidateOption",
			"number": "1.1",
			"lastName": "Vuilleumier",
			"firstName": "Sebastian",
			"sex": "M",
			"yearOfBirth": 1988,
			"studyBranch": "Humanmedizin",
			"studyDegree": "MA",
			"studySemester": 10,
			"status": "OLD"
		},
		{
			"id": 3,
			"type": "candidateOption",
			"number": "1.2",
			"lastName": "Schmid",
			"firstName": "Luca",
			"sex": "M",
			"yearOfBirth": 1993,
			"studyBranch": "Humanmedizin",
			"studyDegree": "BA",
			"studySemester": 2,
			"status": "OLD"
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
			"optionIds": [2, 3, 6],
			"lowerBound": 0,
			"upperBound": 40
		},
		{
			"id": 4,
			"type": "cumulation",
			"optionIds": [2, 3, 6],
			"lowerBound": 0,
			"upperBound": 3
		}
	],
	"issues": [
		{
			"id": 1,
			"type": "listElection",
			"title": {
				"de": "Wahlen des SUB-StudentInnenrates 2015",
				"fr": "Élection du conseil des étudiant-e-s SUB 2015",
				"en": "SUB Elections 2015"
			},
			"description": {
				"de": "Der Wahlzettel kann einer Liste zugeordnet werden und maximal 40 Kandidierende aus verschiedenen Listen enthalten. Einzelne Kandidierende können bis zu dreimal aufgeführt werden. Enthält ein Wahlzettel weniger als 40 Kandidierende, so zählen die fehlenden Einträge als Zusatzstimmen für die ausgewählte Liste. Wenn keine Liste angegeben ist, verfallen diese Stimmen.",
				"fr": "Le bulletin de vote peut comporter au maximum 40 candidats. Il peut comporter des candidats de listes différentes. Chaque électeur peut cumuler jusqu’à trois suffrages sur un candidat. Le bulletin de vote peut-être attribué à une liste. Si un bulletin de vote contient un nombre de candidats inférieurs à 40, les lignes laissées en blanc sont considérées comme autant de suffrages complémentaires attribués à la liste choisie. Si aucune liste n’est indiquée, ces suffrages  sont considérés comme périmés.",
				"en": "Your ballot paper can include up to 40 candidates from all lists. It can include candidates from different lists. You may vote for each candidate up to three times. You can assign a list to your ballot. If you vote for less than 40 candidates, the missing entries will count as list votes for the chosen list. If you do not assign a list, they will expire and count as empty votes."
			},
			"optionIds": [1, 2, 3, 5, 6],
			"ruleIds": [1, 2, 3, 4]
		}
	],
	"ballotEncoding": "E1"
};



describe('SummationRuleTest', function () {
	it('creating and verifying 1', function () {
		var rule = new SummationRule(summationRule);
		var map = [];
		map[1] = 1;
		map[23] = 2;
		var v = rule.verify(map);
		expect(v).toEqual(Rule.SUCCESS);
	});
	it('creating and verifying 2', function () {
		var rule = new SummationRule(summationRule);
		var map = [];
		map[1] = 2;
		map[23] = 4;
		var v = rule.verify(map);
		expect(v).toEqual(Rule.ERROR_SUMMATION_UPPER);
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
		expect(v).toEqual(Rule.SUCCESS);
	});
	it('creating and verifying (upperBound) 2', function () {
		var rule = new CumulationRule(cumulationRule);
		var map = [];
		map[1] = 2;
		map[23] = 4;
		var v = rule.verify(map);
		expect(v).toEqual(Rule.ERROR_CUMULATION_UPPER);
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
		expect(v).toEqual(Rule.ERROR_CUMULATION_LOWER);
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


describe('ElectionDetailsTest', function () {
	it('creating and verifying vote', function () {
		var details = new ElectionDetails(listElectionDetails);
		details.addChoice(1, 1);
		details.addChoice(2, 2);
		var v = details.verifyVote();
		expect(v).toEqual(Rule.SUCCESS);
		details.addChoice(3, 10);
		v = details.verifyVote();
		expect(v).toEqual(Rule.ERROR_CUMULATION_UPPER);
		details.removeChoice(3);
		v = details.verifyVote();
		expect(v).toEqual(Rule.SUCCESS);
	});
});

describe('ListElectionDetailsTest', function () {
	it('creating and retreiving lists and listCandidates', function () {
		var details = new ElectionDetails(listElectionDetails);
		var issue = details.issues[0];
		var lists = issue.getLists();
		expect(lists[0].id).toEqual(1);
		expect(lists[1].id).toEqual(5);
		var candidates = issue.getListCandidates(1);
		expect(candidates[0].id).toEqual(2);
		expect(candidates[1].id).toEqual(2);
		expect(candidates[2].id).toEqual(3);
	});

	it('lists are choosable', function () {
		var details = new ElectionDetails(listElectionDetails);
		var issue = details.issues[0];
		var listsAreChoosable = issue.listsAreChoosable();
		expect(listsAreChoosable).toEqual(true);

		var listElectionDetails2 = jQuery.extend(true, {}, listElectionDetails);
		listElectionDetails2.rules[0].upperBound = 0;
		listElectionDetails2.rules[1].upperBound = 0;

		var details = new ElectionDetails(listElectionDetails2);
		var issue = details.issues[0];
		var listsAreChoosable = issue.listsAreChoosable();
		expect(listsAreChoosable).toEqual(false);

	});
});
