#!/bin/bash

set -e -x

SED_EXT=-r
case "$(uname)" in
Darwin*)
        SED_EXT=-E
esac
export SED_EXT

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"
DATA="${PROJECT}/data"
mkdir -p "${DATA}"
PREFIX="${DATA}/test-$$-"

function clean-up() {
    rm -rf "${PREFIX}"*
}

trap "clean-up" 0

FLAT='false'
if type json-to-flat.sh > /dev/null 2>&1
then
    FLAT='true'
fi

function post-process() {
    if "${FLAT}"
    then
        json-to-flat.sh -p "$@" -
    else
        cat
    fi
}

IMPORT_FILE="${PROJECT}/../../../Installation/BeliefSystem.zip"
if [ -r "${IMPORT_FILE}" ]
then
    echo "Found import file: [${IMPORT_FILE}]"
    curl -sS -D - -X DELETE 'http://localhost:8888/beliefsystem-rest/epistemics/belief-system'
    curl -sS -D - -X POST -F "file=@${IMPORT_FILE}" 'http://localhost:8888/beliefsystem-rest/epistemics/belief-system'
fi

function curl-rest() {
    local URL_PATH="$1"
    shift
    local URL="http://localhost:8888${URL_PATH}"
    echo "URL=[${URL}]" >&2
    local JSON_MIME='application/json'
    curl -sS -D - --header "Content-Type: ${JSON_MIME}" --header "Accept: ${JSON_MIME}" "$@" "${URL}" | tr -d '\015'
}
function post-appraisal() {
    local URL_PATH="$1"
    shift
    curl-rest "/mentalworld-rest/epistemics/appraisal${URL_PATH}" "$@" -X POST
}
function get-appraisal() {
    local URL_PATH="$1"
    shift
    curl-rest "/mentalworld-rest/epistemics/appraisal${URL_PATH}" "$@" -X GET
}
function post-node() {
    local URL_PATH="$1"
    shift
    curl-rest "/mentalworld-rest/epistemics-node/node${URL_PATH}" "$@" -X POST
}
function get-node() {
    local URL_PATH="$1"
    shift
    curl-rest "/mentalworld-rest/epistemics-node/node${URL_PATH}" "$@" -X GET
}

ENGINE_SETTINGS='{
  "believeDeviation": {
    "criterion": 0.35
  },
  "changeConcept": {
    "newAssociationTruthValue": 0.5
  },
  "contextAssociationMaximumDistance": 1.14,
  "epistemicAppraisal": {
    "criterion": 0.3
  },
  "fiction": {
    "cutoff": 2,
    "deviation": 0.15,
    "mean": 0.25
  },
  "insecurity": {
    "converseToTarget": 0.45,
    "directAssociationModificationPercentage": 21
  },
  "integratorDeviation": {
    "criterion": 0.5
  },
  "maximumNumberOfTraversals": 2,
  "metaphor": {
    "intersectionMinimumSize": {
      "absolute": 2,
      "relative": 10
    },
    "intersectionMinimumSizeMixed": {
      "absolute": 2,
      "relative": 10
    },
    "vicinity": 0.5
  },
  "reality": {
    "cutoff": 2,
    "deviation": 0.15,
    "mean": 0.75
  },
  "reassurance": {
    "directAssociationModificationPercentage": 20,
    "indirectAssociationsModificationPercentage": 5
  }
}'

COOKIE_JAR="${PREFIX}cookies"

function create-appraisal() {
    post-appraisal '' -c "${COOKIE_JAR}" | sed -n -e 's/^[Ll]ocation:.*\///p'
}

APPRAISAL_ID="$(create-appraisal)"
if [ -z "${APPRAISAL_ID}" ]
then
    echo "No appraisal ID" >&2
    exit -1
fi

echo "APPRAISAL_ID=[${APPRAISAL_ID}]"

curl-rest '/mentalworld-rest/epistemics/context' -X DELETE

post-appraisal "/${APPRAISAL_ID}/engine-settings" -b "${COOKIE_JAR}" -d "${ENGINE_SETTINGS}"
get-appraisal "/${APPRAISAL_ID}/engine-settings" -b "${COOKIE_JAR}" \
    | post-process

post-appraisal "/${APPRAISAL_ID}/observation-features" -b "${COOKIE_JAR}" -d '["beak", "eggs"]'

get-appraisal "/${APPRAISAL_ID}/observation-features" -b "${COOKIE_JAR}" \
    | post-process -a

curl -sS -D - "http://localhost:8888/mentalworld-rest/epistemics/appraisal/${APPRAISAL_ID}/new-context" -b "${COOKIE_JAR}" -d 'conceptId=circus'

get-appraisal "/${APPRAISAL_ID}/new-context" -b "${COOKIE_JAR}" \
    | post-process

post-node "/${APPRAISAL_ID}/decide/CategoryMatch" -b "${COOKIE_JAR}" | sed "${SED_EXT}" -e '$s/^/Result: /'

post-node "/${APPRAISAL_ID}/decide/ContextMatch" -b "${COOKIE_JAR}" | sed "${SED_EXT}" -e '$s/^/Result: /'

post-node "/${APPRAISAL_ID}/action/DeclareContext" -b "${COOKIE_JAR}" | sed "${SED_EXT}" -e '$s/^/Result: /'

post-node "/${APPRAISAL_ID}/decide/CategoryMatch" -b "${COOKIE_JAR}" | sed "${SED_EXT}" -e '$s/^/Result: /'

get-appraisal "/${APPRAISAL_ID}/log-messages" -b "${COOKIE_JAR}" \
    | post-process -a
